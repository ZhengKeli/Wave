pluginManagement {
	repositories {
		maven(uri("https://maven.aliyun.com/repository/gradle-plugin"))
		mavenCentral()
	}
}

include(":core", ":core-aparapi")
include(":app")
