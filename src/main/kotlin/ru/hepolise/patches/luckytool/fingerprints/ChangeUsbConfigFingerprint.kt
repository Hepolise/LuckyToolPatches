package ru.hepolise.patches.luckytool.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object ChangeUsbConfigFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.FINAL or AccessFlags.PUBLIC,
    strings = listOf("changeUsbConfig"),
)