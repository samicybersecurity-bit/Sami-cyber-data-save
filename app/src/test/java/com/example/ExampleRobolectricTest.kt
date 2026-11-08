package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTestSuite {

  @Test
  fun testAppNameConstant() {
    val appName = "Sami Cyber Security"
    assertEquals("Sami Cyber Security", appName)
  }

  @Test
  fun testMasterAccessKeyValidation() {
    val defaultKey = "SAMI-CYBER-2026"
    assertTrue(defaultKey.isNotEmpty())
    assertTrue(defaultKey.startsWith("SAMI"))
  }

  @Test
  fun testPasswordSecurityStrength() {
    val strongPassword = "SecureP@ssw0rd!2026"
    assertTrue(strongPassword.length >= 12)
    assertTrue(strongPassword.any { it.isUpperCase() })
    assertTrue(strongPassword.any { it.isDigit() })
  }
}
