plugins {
	kotlin("jvm")
}

dependencies {
	kotlin("stdlib")
	testImplementation("junit:junit:4.13.2")

	implementation(fileTree("libs") { include("*.jar") })
}
