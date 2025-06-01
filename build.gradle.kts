//	https://docs.gradle.org.cn/
plugins {
    `java-library`
}
//apply(from="version.gradle.kts")

group = "gleam"
version = "1.0-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}
repositories {
    mavenCentral()
}
object Versions{
    val slf4jVersion = "2.0.17"
	val log4jVersion = "2.24.3"
	val nettyVersion = "4.1.119.Final"
	val jacksonVersion = "2.18.3"
}

dependencies {
	//api会传递给子项目 implementation不会 
    //testImplementation(platform("org.junit:junit-bom:5.10.0"))
    //testImplementation("org.junit.jupiter:junit-jupiter")
    // log
    api(group= "org.slf4j", name= "slf4j-api", version= "2.0.17");
    api(group= "org.apache.logging.log4j", name= "log4j-api", version= "${Versions.log4jVersion}")
    api(group= "org.apache.logging.log4j", name= "log4j-core", version= "${Versions.log4jVersion}")
    api(group= "org.apache.logging.log4j", name= "log4j-slf4j2-impl", version= "${Versions.log4jVersion}")
    
    // communication
    api(group= "io.netty", name= "netty-all", version= "${Versions.nettyVersion}")
    api(group= "com.google.protobuf", name= "protobuf-java", version= "4.30.1")
    api(group= "org.apache.httpcomponents.client5", name= "httpclient5", version= "5.4.2")    

    // https://mvnrepository.com/artifact/jakarta.servlet/jakarta.servlet-api
    //compileOnly(group= "jakarta.servlet", name= "jakarta.servlet-api", version= "6.1.0")
    
    // sql
    api(group= "com.mysql", name= "mysql-connector-j", version= "9.2.0")
    api(group= "com.zaxxer", name= "HikariCP", version= "6.2.1")
    api(group= "commons-dbutils", name= "commons-dbutils", version= "1.8.1")
    api(group= "org.redisson", name= "redisson", version= "3.45.0")
    
    // json
    api(group= "com.fasterxml.jackson.core", name= "jackson-core", version= "${Versions.jacksonVersion}")
    api(group= "com.fasterxml.jackson.core", name= "jackson-databind", version= "${Versions.jacksonVersion}")
    api(group= "com.fasterxml.jackson.core", name= "jackson-annotations", version= "${Versions.jacksonVersion}")
    api(group= "com.fasterxml.jackson.dataformat", name= "jackson-dataformat-yaml",version= "${Versions.jacksonVersion}")
    api(group= "com.fasterxml.jackson.datatype", name= "jackson-datatype-jsr310", version= "${Versions.jacksonVersion}")
    
    // tool
    api(group= "commons-io", name= "commons-io", version= "2.18.0")
    api(group= "org.apache.commons", name= "commons-lang3", version= "3.17.0")
    //https://github.com/google/guava/issues/6825
    api(group= "com.google.guava", name= "guava", version= "33.4.0-jre")
    
    api(group= "org.apache.groovy", name= "groovy-jsr223", version= "4.0.26")
    
	//直接引用libs目录下的jar包
    api(fileTree("libs"))
}

tasks.test {
    useJUnitPlatform()
}