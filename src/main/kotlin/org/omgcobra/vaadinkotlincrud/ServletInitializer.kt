package org.omgcobra.vaadinkotlincrud

import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

class ServletInitializer : SpringBootServletInitializer() {

    override fun configure(application: SpringApplicationBuilder) = application.sources(VaadinKotlinCrudApplication::class.java)

}
