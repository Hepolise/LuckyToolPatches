rootProject.name = "lucky-tool-patches"

buildCache {
    local {
        isEnabled = "CI" !in System.getenv()
    }
}
