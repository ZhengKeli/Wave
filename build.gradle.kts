plugins {
	kotlin("jvm") version "1.7.0" apply false
}

subprojects {
	repositories {
		maven(uri("https://maven.aliyun.com/repository/public"))
		mavenCentral()
	}
}
