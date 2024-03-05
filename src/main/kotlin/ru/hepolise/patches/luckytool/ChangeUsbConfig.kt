package ru.hepolise.patches.luckytool

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import ru.hepolise.patches.luckytool.fingerprints.ChangeUsbConfigFingerprint

@Patch(
    name = "Change Usb Config patch",
    description = "This patch forces LuckyTool to start a USB tethering mode.",
    compatiblePackages = [
        CompatiblePackage("com.luckyzyx.luckytool"),
    ],
)
@Suppress("unused")
object ChangeUsbConfig : BytecodePatch(
    setOf(ChangeUsbConfigFingerprint)
) {

    private val OPCODES = listOf(
        Opcode.CONST_STRING,
        Opcode.IPUT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL_RANGE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.FILLED_NEW_ARRAY,
    )

    private val PATCHABLE_STRING_BLOCS_START = listOf(
        "onUsbSelect",
        "updateUsbNotification",
        "changeUsbConfig"
    )


    override fun execute(context: BytecodeContext) {
        ChangeUsbConfigFingerprint.result?.apply {
            println("found method: " + this.method.name)
            var lastIndex = 0
            var reference = ""
            for (instruction in mutableMethod.getInstructions()) {
                if (OPCODES[lastIndex] == instruction.opcode) {
                    lastIndex++
                    if (instruction.opcode === Opcode.CONST_STRING) {
                        val stringInstruction = instruction as BuilderInstruction21c
                        reference = stringInstruction.reference.toString()
                        if (!PATCHABLE_STRING_BLOCS_START.contains(reference)) {
                            lastIndex = 0
                        }
                    }
                } else {
                    lastIndex = 0
                }
//                println("lastIndex: $lastIndex")
                if (lastIndex == OPCODES.size) {
                    println("found instruction of $reference at " + instruction.location.index)
                    val filledNewArrayInstr = instruction as BuilderInstruction35c
                    val lastSmaliInstr = if (filledNewArrayInstr.registerCount == 1) {
                        "filled-new-array {v9}, [Ljava/lang/Object;"
                    } else {
                        "filled-new-array {v${filledNewArrayInstr.registerC}, v9}, [Ljava/lang/Object;"
                    }
                    mutableMethod.addInstructions(
                        instruction.location.index,
                        """
                        const/4 v9, 0x3
                        invoke-static {v9}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                        move-result-object v9
                        """
                    )
                    mutableMethod.replaceInstructions(instruction.location.index, lastSmaliInstr)
                    lastIndex = 0
                }
//                if (lastIndex > 0) {
//                    println("instruction: ${instruction.opcode}")
//                    println("location: ${instruction.location.index}")
//                }
            }
//            mutableMethod.addInstructions()

        } ?: throw PatchException("Failed to resolve ${this.javaClass.simpleName}")
    }
}
