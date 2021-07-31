plugins {
	kotlin("jvm")
	id("application")
	id("org.openjfx.javafxplugin") version "0.0.9"
}

dependencies {
	kotlin("stdlib")
	testImplementation("junit:junit:4.12")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.5.1")
	implementation("no.tornado:tornadofx:1.7.20")

	implementation(project(":core"))
	implementation(project(":core-aparapi"))
}

javafx {
	version = "11.0.2"
	modules = listOf("javafx.controls", "javafx.swing")
}
