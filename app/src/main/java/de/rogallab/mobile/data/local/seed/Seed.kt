package de.rogallab.mobile.data.local.seed

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import de.rogallab.mobile.R
import de.rogallab.mobile.data.dtos.PersonDto
import de.rogallab.mobile.data.local.io.deleteFileOnStorage
import de.rogallab.mobile.data.local.io.writeImageToStorage
import de.rogallab.mobile.domain.utilities.createUuid
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logVerbose
import kotlin.random.Random

class Seed(
   private val context: Context,
   private val resources: Resources
) {
   private val _imagesPath = mutableListOf<String>()

   var personDtos: MutableList<PersonDto> = mutableListOf()
   //--- P E O P L E -----------------------------------------------------------------------
   fun createPerson(withImages: Boolean): Seed {

      val firstNames = mutableListOf(
         "Arne", "Berta", "Cord", "Dagmar", "Ernst", "Frieda", "Günter", "Hanna",
         "Ingo", "Johanna", "Klaus", "Luise", "Martin", "Nadja", "Otto", "Patrizia",
         "Quirin", "Rebecca", "Stefan", "Tanja", "Uwe", "Veronika", "Walter", "Xaver",
         "Yvonne", "Zwantje")
      val lastNames = mutableListOf(
         "Arndt", "Bauer", "Conrad", "Diehl", "Engel", "Fischer", "Graf", "Hoffmann",
         "Imhoff", "Jung", "Klein", "Lang", "Meier", "Neumann", "Olbrich", "Peters",
         "Quart", "Richter", "Schmidt", "Thormann", "Ulrich", "Vogel", "Wagner", "Xander",
         "Yakov", "Zander")
      val emailProvider = mutableListOf("gmail.com", "icloud.com", "outlook.com", "yahoo.com",
         "t-online.de", "gmx.de", "freenet.de", "mailbox.org", "yahoo.com", "web.de")
      val random = Random(0)
      for (index in firstNames.indices) {
         val firstName = firstNames[index]
         val lastName = lastNames[index]
         val email =
            "${firstName.lowercase()}." +
               "${lastName.lowercase()}@" +
               "$emailProvider.random()"
         val phone =
            "0${random.nextInt(1234, 9999)} " +
               "${random.nextInt(100, 999)}-" +
               "${random.nextInt(10, 9999)}"
         val personDto = PersonDto(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
            localImage = null,
            remoteImage = null,
            id = createUuid(index + 1, 1)
         )
         logVerbose("<-Seed", "personDto: $personDto")
         personDtos.add(personDto)
      }

   //--- I M A G E S -----------------------------------------------------------------------
      if (!withImages) return this
      // convert the drawables into image files
      val drawables = mutableListOf<Int>()
      drawables.add(0, R.drawable.man_1)
      drawables.add(1, R.drawable.man_2)
      drawables.add(2, R.drawable.man_3)
      drawables.add(3, R.drawable.man_4)
      drawables.add(4, R.drawable.man_5)
      drawables.add(5, R.drawable.man_6)
      drawables.add(6, R.drawable.woman_1)
      drawables.add(7, R.drawable.woman_2)
      drawables.add(8, R.drawable.woman_3)
      drawables.add(9, R.drawable.woman_4)
      drawables.add(10, R.drawable.woman_5)

      drawables.forEach { it: Int ->  // drawable id
         val bitmap = BitmapFactory.decodeResource(resources, it)
         bitmap?.let { itbitm ->
            writeImageToStorage(context, itbitm)?.let { uriPath: String? ->
               uriPath?.let { _imagesPath.add(uriPath) }
            }
         }
      }
      if (_imagesPath.size == 11) {
         personDtos[0] = personDtos[0].copy(localImage = _imagesPath[0])
         personDtos[1] = personDtos[1].copy(localImage = _imagesPath[6])
         personDtos[2] = personDtos[2].copy(localImage = _imagesPath[1])
         personDtos[3] = personDtos[3].copy(localImage = _imagesPath[7])
         personDtos[4] = personDtos[4].copy(localImage = _imagesPath[2])
         personDtos[5] = personDtos[5].copy(localImage = _imagesPath[8])
         personDtos[6] = personDtos[6].copy(localImage = _imagesPath[3])
         personDtos[7] = personDtos[7].copy(localImage = _imagesPath[9])
         personDtos[8] = personDtos[8].copy(localImage = _imagesPath[4])
         personDtos[9] = personDtos[9].copy(localImage = _imagesPath[10])
         personDtos[10] = personDtos[10].copy(localImage = _imagesPath[5])
      }
      return this
   }

   fun disposeImages() {
      _imagesPath.forEach { imageUrl ->
         logDebug("<disposeImages>", "Url $imageUrl")
         deleteFileOnStorage(imageUrl)
      }
   }
}