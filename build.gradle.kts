// This makes classes available in the current scope that are useful for java projects, among which the task class "Jar", which is used later in this file to create jar files. More information: https://docs.gradle.org/current/userguide/java_library_plugin.html#java_library_plugin
plugins {
    id("java")
}

//todo: where is this used?
version = "0.1"

// This means: search for the dependencies (listed below) in maven central.
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation("org.jetbrains:annotations:24.0.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation("org.antlr:antlr4-runtime:4.13.0")
    implementation("com.fifesoft:rsyntaxtextarea:3.3.4")
}

//This causes JUnit 5 to be used. Without this, gradle assumes JUint 4.
tasks.test {
    useJUnitPlatform()
}


//
// Taken
//
//Alles hierboven zijn standaarddingen die bijna ieder gradleproject nodig heeft. Wat nu volgt zijn eigengedefinieerde taken om jarbestanden te maken.

//De taken maken gebruik van het bestaan van de sourceSet "main". Die bestaat altijd. Je kunt ook zelf extra sourceSets definiëren, maar standaard bestaan altijd "main" en "test". Gradle veronderstelt daarbij ook een bepaalde mappenstructuur, namelijk:
/*
src
    main
        java
        resources
    test
        java
        resources
 */
//In de mappen genaamd java moet de broncode staan. Als je die structuur hebt werkt alles automatisch. Zo niet, dan kun je gradle ook wel configureren om de broncode ergens anders te zoeken, maar dat doe ik hier niet. Ik gebruik de standaard mappenstructuur.

//Iedere taak heeft een naam. De jargenererende taken hebben de namen producer_jar en assessor_jar. Uitvoeren van een taak gaat met een dergelijk commando:
//gradle producer_jar

arrayOf("producer", "assessor", "devtools.explorer").forEach {
    tasks.register<Jar>("${it}_jar")
    {
        // have Gradle warn on possible duplicates, but continue anyway
        duplicatesStrategy = DuplicatesStrategy.WARN

        //waar zit de mainfunctie?
        manifest {
            attributes.set("Main-Class", "nl.ou.debm.${it}.Main")
        }
        archiveFileName.set("${it}.jar")

        //Alles vanaf hier snap ik niet echt:
        from(sourceSets.getByName("main").output)
        dependsOn(configurations.runtimeClasspath)
        //Volgens mij zorgt dit ervoor dat als je bibliotheken gebruikt die in een jarbestand zijn ingepakt, ook die jarbestanden ìn het jarbestand van debm komen, maar ik heb dit niet getest.
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }
}
