plugins {
	kotlin("jvm")
}

dependencies {
	kotlin("stdlib")
	testImplementation("junit:junit:4.12")

	implementation(fileTree("libs") { include("*.jar") })
}
