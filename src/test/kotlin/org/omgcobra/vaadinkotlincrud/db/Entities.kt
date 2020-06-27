package org.omgcobra.vaadinkotlincrud.db

import io.kotest.assertions.throwables.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonTest {
    @Test
    fun `Entity get method should work`() {
        val person = Person().apply {
            firstName = "John"
        }

        shouldNotThrowAny {
            person[Person::firstName.name] shouldBe "John"
        }
    }

    @Test
    fun `Entity set method should work on a mutable field`() {
        val person = Person().apply {
            firstName = "John"
        }

        shouldNotThrowAnyUnit {
            person[Person::firstName.name] = "Steve"
        }

        person.firstName shouldBe "Steve"
    }

    @Test
    fun `Entity set method should fail on an immutable field`() {
        val person = Person(id = 1)

        shouldNotThrowAnyUnit {
            person[Person::id.name] = 2
        }

        person.id shouldBe 1
    }

    @Test
    fun `Entity creation should work`() {
        val person = Person::class.primaryConstructor?.callBy(
                mapOf(Person::class.primaryConstructor!!.findParameterByName(Person::id.name)!! to 3)
        )

        person?.id shouldBe 3
    }

    @Test
    fun `Entity keys should work`() {
        val person = Person(id = 4)

        person.keys shouldBe listOf(PropertyResult(name = "id", value = 4))
    }
}