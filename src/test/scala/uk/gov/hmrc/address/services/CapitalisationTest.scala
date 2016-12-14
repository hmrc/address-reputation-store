/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.address.services

import org.scalatest.FunSuite

class CapitalisationTest extends FunSuite {
  import Capitalisation._

  def tryNormaliseAddressLine(expected: String) {
    assert(normaliseAddressLine(expected.toUpperCase) === expected)
    assert(normaliseAddressLine(expected.toLowerCase) === expected)
  }


  test(
    """edge cases""") {
    assert(normaliseAddressLine("") === "", "blank")
    assert(normaliseAddressLine("  ") === "", "blank")
    assert(normaliseAddressLine("UNITS 2 - 5, MANOR COURTYARD") === "Units 2 - 5, Manor Courtyard", "spaces surrounding hyphen")
    assert(normaliseAddressLine("- - - -") === "- - - -", "alternating hyphens and spaces")
    assert(normaliseAddressLine(" - - - ") === "- - -", "alternating spaces and hyphens")
  }

  test(
    """Given an address line with duplicate spaces,
      when normalised,
      then the duplicate spaces should be reduced
    """) {
    assert(normaliseAddressLine("78  Rivermead  Road") === "78 Rivermead Road") // EX2 4RL
  }

  test(
    """Given a town in uppercase or lowercase and includes spaces,
      when normalised,
      then the town should be proper case
      and stop-words should remain lowercase
      except when they are the first word
    """) {
    // English
    tryNormaliseAddressLine("Ashby de la Zouch")
    tryNormaliseAddressLine("St. Leonards Hill")
    tryNormaliseAddressLine("Newcastle upon Tyne")
    tryNormaliseAddressLine("Bridge of Don")
    tryNormaliseAddressLine("Sutton in Ashfield")
    tryNormaliseAddressLine("Barn in the Wood")
    tryNormaliseAddressLine("Magazine 1 to 3 and 7")
    // Welsh
    tryNormaliseAddressLine("Pen y Bryn")
    tryNormaliseAddressLine("Tan yr Allt")
    tryNormaliseAddressLine("Allt yr Yn")
    tryNormaliseAddressLine("Y Bala")
    tryNormaliseAddressLine("Yr Wyddgrug")
    // Gaelic
    tryNormaliseAddressLine("Machair an Sgitheach")
    tryNormaliseAddressLine("An Gearasdan")
    tryNormaliseAddressLine("Tigh an Rubha")
    tryNormaliseAddressLine("Taigh nam Broc")
    tryNormaliseAddressLine("Port na Cloiche")
  }

  ignore(
    """Given a street name starting with 'The',
      when normalised,
      then the definite article should be capitalised.
    """) {
    tryNormaliseAddressLine("The Copse")
    tryNormaliseAddressLine("221b The Copse")
  }

  test(
    """Given a town in uppercase or lowercase and includes dashes,
      when normalised,
      then the town should be proper case
      and stop-words should remain lowercaseq
      except when they are the first word
    """) {
    tryNormaliseAddressLine("55a-57c") // flat ranges are common
    tryNormaliseAddressLine("Brightwell-cum-Sotwell") // OX10 0RZ
    tryNormaliseAddressLine("Chapel-en-le-Frith")
    tryNormaliseAddressLine("Court-at-Street") // CT21 4PQ
    tryNormaliseAddressLine("Crows-an-Wra") // TR19 6HS
    tryNormaliseAddressLine("Flat 1, Dan-y-Graig") // SA12 8DX
    tryNormaliseAddressLine("Fleur-de-Lis") // NP12 3TS
    tryNormaliseAddressLine("Havering-Atte-Bower") // RM4 1QR
    tryNormaliseAddressLine("Newbiggin by-the-Sea")
    tryNormaliseAddressLine("Newcastle-under-Lyme")
    tryNormaliseAddressLine("Normanby-by-Spital") // LN8 2FS
    tryNormaliseAddressLine("Pen-y-Bont")
    tryNormaliseAddressLine("Rixton-with-Glazebrook") // WA3 5BG
    tryNormaliseAddressLine("Southend-on-Sea")
    tryNormaliseAddressLine("Wells-next-the-Sea") // NR23 1EQ
    tryNormaliseAddressLine("Whip-Ma-Whop-Ma-Gate") // YO1 8BL
    tryNormaliseAddressLine("1 Whip-Ma-Whop-Ma-Gate") // YO1 8BL also
  }

  test(
    """Given a town in uppercase or lowercase and includes apostrophes,
      when normalised,
      then the town should be proper case without caring about the apostrophes
    """) {
    tryNormaliseAddressLine("Bo'ness")
    tryNormaliseAddressLine("Pentre'r Bryn")
    tryNormaliseAddressLine("Godre'r Graig")
    tryNormaliseAddressLine("Pen Isa'r Lôn")
    tryNormaliseAddressLine("Tre'r Ddol")
    tryNormaliseAddressLine("Caretaker's Flat")
    tryNormaliseAddressLine("Well I' Th' Lane") // OL11 1AU/1BL/1BB/2JR
  }

  test(
    """Given a special case,
      when normalised,
      then the name should be presented correctly
    """) {
    tryNormaliseAddressLine("I'Anson Street") // DL3 0RL
    tryNormaliseAddressLine("11 I'Anson Street") // DL3 0RL
    tryNormaliseAddressLine("BFPO 123")
  }

  test(
    """Given a town in uppercase or lowercase and includes a contracted prefix,
      when normalised,
      then the town should be proper case
      and both letters either side of the apostrophe should be uppercase
    """) {
    tryNormaliseAddressLine("John O'Groats")
    tryNormaliseAddressLine("Tolleshunt D'Arcy")
    tryNormaliseAddressLine("Kincardine O'Neil")
    tryNormaliseAddressLine("Allt A'Tuath")
    tryNormaliseAddressLine("Tu Hwnt I'r Bwlch") // LL49 9PA
  }

  test(
    """Given a town in uppercase or lowercase and includes a contracted suffix,
      when normalised,
      then the town should be proper case
      and both letters either side of the apostrophe should be uppercase
    """) {
    tryNormaliseAddressLine("5 Top O'Th' Hill Road") // OL14 6QA
  }

  test(
    """Given an organisation in upper or lowercase and includes spaces,
      when normalised,
      then the name should be proper case
    """) {
    tryNormaliseAddressLine("R D Taylor & Co Ltd")
  }

  test(
    """Given a special-case acronym,
      when normalised,
      then the acronym should be all-uppercase
    """) {
    tryNormaliseAddressLine("BFPO")
  }

}
