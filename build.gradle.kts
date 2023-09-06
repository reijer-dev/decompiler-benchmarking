//De plugin java bevat handige dingen voor javaprojecten. Het effect van deze opdracht is dat er klasses beschikbaar komen in deze scope, bijvoorbeeld de taakklasse Jar, die ik later gebruik om jarbestanden mee te maken. Meer informatie over wat de plugin doet: https://docs.gradle.org/current/userguide/java_library_plugin.html#java_library_plugin
plugins {
    id("java")
}

//waar wordt dit gebruikt?
version = "0.1"

//Dit bepaalt waar gradle de afhankelijkheden gaat zoeken die bij "dependencies" staan aangegeven.
repositories {
    mavenCentral()
}

//Wat is dat voor notatie in die strings? Is dit hoe maven central bibliotheken identificeert?
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

//zorgt dat JUnit 5 gebruikt wordt. Zonder dit gaat gradle standaard uit van JUnit 4.
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

arrayOf("producer", "assessor").forEach {
    tasks.register<Jar>("${it}_jar")
    {
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