package com.example.springsandbox.jpa

import com.example.springsandbox.configuration.jpa.JpaSliceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@JpaSliceTest
internal class SmokeJpaSliceTest {

    @Test
    internal fun smokeTest() {
        assertThat(true).isTrue
    }

}
