plugins {
	java
	kotlin("jvm")
}

dependencies {
	kotlin("stdlib")
	testImplementation("junit:junit:4.12")

	implementation(project(":core"))
	implementation("com.aparapi:aparapi:1.8.0")
}
