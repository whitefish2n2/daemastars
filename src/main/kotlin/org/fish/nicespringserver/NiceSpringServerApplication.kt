package org.fish.nicespringserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NiceSpringServerApplication

fun main(args: Array<String>) {
    runApplication<NiceSpringServerApplication>(*args)
}
