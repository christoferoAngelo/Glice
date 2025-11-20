plugins {
    // geralmente vazio
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
