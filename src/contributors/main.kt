package contributors

fun main() {
    setDefaultFontSize(16f)
    ContributorsUI().apply {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
    }
}