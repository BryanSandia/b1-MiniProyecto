// =====================================================================
// MINI PROYECTO SCALA: Higher Order Functions en Criptografía
// ---------------------------------------------------------------------
// Motor de cifrado funcional que recibe una FUNCIÓN DE TRANSFORMACIÓN
// como parámetro (función de orden superior), implementando dos métodos
// clásicos: Cifrado César (desplazamiento) y Cifrado XOR (lógico).
//
// Estilo: programación funcional PURA
//   - Sin `var`, sin ciclos imperativos (`for`/`while`)
//   - `map`, `foldLeft`, `exists`, `forall`, `count`, `maxBy` ...
//   - Manejo de errores con `Try/Success/Failure` y `Either/Left/Right`
//   - Funciones puras: mismo input => mismo output, sin efectos secundarios
//
// Compilar : scalac Criptografia.scala
// Ejecutar : scala CriptoApp
// =====================================================================

import scala.util.{Try, Success, Failure}
import scala.language.implicitConversions

// ---------------------------------------------------------------------
// MODELO DE DATOS con case class (semántica clara e inmutabilidad)
// Un "ResultadoCifrado" empaqueta todo lo que produce una operación:
// mensaje original, resultado, algoritmo usado y clave aplicada.
// ---------------------------------------------------------------------
case class ResultadoCifrado(
    original: String,
    procesado: String,
    algoritmo: String,
    clave: Int
)

// Validación tipada con Either: Left = error descriptivo, Right = valor ok
case class ValidacionError(mensaje: String)

object Cripto {

  // -------------------------------------------------------------------
  // CONSTANTES DEL ALFABETO INGLÉS MAYÚSCULAS (A=0, B=1, ..., Z=25)
  // -------------------------------------------------------------------
  val TamAlfabeto: Int = 26
  val ClaveMaxima: Int = 1000 // límite razonable para considerar válida una clave

  // -------------------------------------------------------------------
  // VALIDACIONES (funciones puras que devuelven Either / Booleanos)
  // -------------------------------------------------------------------

  /** Valida que la cadena no sea nula ni vacía. Either[String, String] */
  def validarMensaje(mensaje: String): Either[String, String] =
    if (mensaje == null) Left("ERROR: el mensaje es nulo (null)")
    else if (mensaje.trim.isEmpty) Left("ERROR: el mensaje esta vacio")
    else Right(mensaje)

  /** Valida que la clave este dentro del rango permitido. Either[String, Int] */
  def validarClave(clave: Int): Either[String, Int] =
    if (clave.abs > ClaveMaxima)
      Left(s"ERROR: clave $clave fuera de rango (|k| debe ser <= $ClaveMaxima)")
    else Right(clave)

  /** Verifica que TODOS los caracteres sean letras mayúsculas A-Z (César).
    * Uso intensivo de `forall` con lambda concisa placeholder `_` */
  def sonLetrasMayusculas(mensaje: String): Boolean =
    mensaje.forall(c => c >= 'A' && c <= 'Z')

  /** Cuenta cuántos caracteres NO soportados tiene un mensaje para César.
    * Uso de `count` + lambda con negación */
  def caracteresNoSoportados(mensaje: String): Int =
    mensaje.count(c => !(c >= 'A' && c <= 'Z'))

  // -------------------------------------------------------------------
  // ALGORITMO 1: CIFRADO CÉSAR (DESPLAZAMIENTO)
  // Fórmula: E(x, k) = (pos(x) + k) mod 26
  // Solo funciona con letras mayúsculas del alfabeto inglés.
  // El operador `mod` real de Scala es `%`; se normaliza a positivo
  // sumando el tamaño del alfabeto para permitir claves negativas
  // (descifrado) sin producir índices negativos.
  // -------------------------------------------------------------------
  def cifradoCesar(caracter: Char, clave: Int): Char =
    Try {
      // Convertir carácter a índice (A=0 ... Z=25)
      val posicion: Int = caracter.toInt - 'A'.toInt
      // Aplicar la fórmula E(x,k) = (pos(x) + k) mod 26 con módulo positivo
      val desplazada: Int = ((posicion + clave) % TamAlfabeto + TamAlfabeto) % TamAlfabeto
      // Convertir el índice resultante de vuelta a carácter
      ('A'.toInt + desplazada).toChar
    } match
      case Success(c) => c
      case Failure(_) =>
        throw new IllegalArgumentException(
          s"Caracter no soportado por Cesar: '$caracter' (solo se admiten A-Z)"
        )

  // -------------------------------------------------------------------
  // ALGORITMO 2: CIFRADO XOR (LÓGICO)
  // Fórmula: E(x, k) = ASCII(x) ⊕ k   (operador `^` en Scala)
  // Propiedad fundamental: aplicar la MISMA operación dos veces
  // con la MISMA clave devuelve el valor original, porque
  // (a ⊕ k) ⊕ k = a  (el XOR es su propio inverso / involución).
  // No requiere invertir el signo de la clave, a diferencia de César.
  // -------------------------------------------------------------------
  def cifradoXOR(caracter: Char, clave: Int): Char =
    Try {
      // ASCII(x) ⊕ k usando el operador caret `^`
      (caracter.toInt ^ clave).toChar
    } match
      case Success(c) => c
      case Failure(_) =>
        throw new IllegalArgumentException(
          s"Fallo al aplicar XOR al caracter '$caracter' con clave $clave"
        )

  // -------------------------------------------------------------------
  // FUNCIÓN DE ORDEN SUPERIOR: procesarMensaje
  // Recibe una función de transformación `funcionCifrado: (Char, Int) => Char`
  // como PARÁMETRO y la aplica a cada carácter del mensaje con `map`.
  // Manejo de excepciones elegante con Try/Success/Failure.
  // -------------------------------------------------------------------
  def procesarMensaje(
      mensaje: String,
      clave: Int,
      funcionCifrado: (Char, Int) => Char,
      nombreAlgoritmo: String = "ANONIMO"
  ): Try[ResultadoCifrado] =
    Try {
      // 1) Validaciones previas encadenadas con pattern matching sobre Either
      val validado: Either[String, String] = for
        m <- validarMensaje(mensaje)
        k <- validarClave(clave)
      yield m

      validado match
        case Left(error) => throw new IllegalStateException(error)
        case Right(ok) =>
          // 2) Regla específica del algoritmo: César exige solo A-Z.
          //    Uso de `exists` para detectar caracteres no soportados.
          val requiereSoloMayusculas: Boolean =
            nombreAlgoritmo.toUpperCase.contains("CESAR")
          if (requiereSoloMayusculas && !sonLetrasMayusculas(ok))
            throw new IllegalArgumentException(
              s"Cesar no soporta caracteres especiales/minusculas " +
                s"(${caracteresNoSoportados(ok)} caracter(es) invalidos en \"$ok\")"
            )
          // 3) Núcleo funcional: aplicar la función recibida como parámetro
          //    a CADA carácter usando `map` (sin bucles imperativos).
          val transformado: String = ok.map(c => funcionCifrado(c, clave)).mkString
          ResultadoCifrado(original = ok, procesado = transformado,
                           algoritmo = nombreAlgoritmo, clave = clave)
    }

  // Versión genérica que toma SOLO la función de transformación
  // (demostración pura de pasar funciones como argumentos).
  def transformar(mensaje: String, clave: Int, f: (Char, Int) => Char): String =
    mensaje.map(c => f(c, clave))

  // -------------------------------------------------------------------
  // UTILIDADES FUNCIONALES DE ANÁLISIS (uso de foldLeft / scanLeft /
  // maxBy / minBy / count / map) sobre los resultados de cifrado
  // -------------------------------------------------------------------

  /** Distribución de frecuencias simplificada: cuenta ocurrencias por
    * carácter usando foldLeft sobre un Map inmutable (nunca `var`). */
  def frecuenciaCaracteres(texto: String): Map[Char, Int] =
    texto.foldLeft(Map.empty[Char, Int]) { (acc, c) =>
      acc.updated(c, acc.getOrElse(c, 0) + 1)
    }

  /** Carácter más frecuente de un texto con `maxBy` sobre la frecuencia. */
  def caracterMasFrecuente(texto: String): Option[(Char, Int)] =
    if (texto.isEmpty) None
    else Some(frecuenciaCaracteres(texto).maxBy { case (_, n) => n })

  /** Serie acumulada de códigos ASCII (scanLeft) — útil para inspeccionar
    * cómo evoluciona el mensaje carácter a carácter tras el cifrado. */
  def serieAcumuladaAscii(texto: String): List[Int] =
    texto.scanLeft(0)((acum, c) => acum + c.toInt).toList

  /** Distancia media de desplazamiento entre original y cifrado,
    * calculada con foldLeft y conversión explícita `.toDouble`. */
  def distanciaMedia(original: String, cifrado: String): Double =
    if (original.isEmpty) 0.0
    else
      original
        .zip(cifrado)
        .foldLeft(0.0) { (acc, par) =>
          val (a, b) = par
          acc + math.abs(a.toDouble - b.toDouble)
        } / original.length.toDouble

  /** Comprueba la propiedad de involución del XOR con `forall`:
    * aplicar la MISMA función dos veces con la MISMA clave devuelve
    * el mensaje original, carácter a carácter. */
  def esInvolucion(mensaje: String, clave: Int): Boolean =
    mensaje
      .zip(mensaje.map(c => cifradoXOR(cifradoXOR(c, clave), clave)))
      .forall { case (original, reconstruido) => original == reconstruido }

  /** Menor desplazamiento ASCII entre dos cadenas (`minBy`) — análisis extra. */
  def caracterMenosDesplazado(original: String, cifrado: String): Option[Char] =
    if (original.isEmpty || cifrado.isEmpty) None
    else Some(original.zip(cifrado).minBy { case (a, b) => math.abs(a.toInt - b.toInt) }._1)
}

// ---------------------------------------------------------------------
// APLICACIÓN PRINCIPAL: casos de prueba obligatorios con println
// ---------------------------------------------------------------------
object CriptoApp {
  import Cripto.*

  // Helper de presentación: imprime un Try con pattern matching Success/Failure
  def mostrar(etiqueta: String, resultado: Try[ResultadoCifrado]): Unit =
    println(s"\n>>> $etiqueta")
    resultado match
      case Success(r) =>
        println(s"    Algoritmo : ${r.algoritmo}")
        println(s"    Original  : \"${r.original}\"")
        println(s"    Clave     : k=${r.clave}")
        println(s"    Resultado : \"${r.procesado}\"")
      case Failure(e) =>
        println(s"    FAILURE capturado elegantemente -> ${e.getClass.getSimpleName}: ${e.getMessage}")

  def main(args: Array[String]): Unit =
    println("=" * 70)
    println("  MOTOR DE CIFRADO FUNCIONAL - HIGHER ORDER FUNCTIONS EN SCALA")
    println("=" * 70)

    // =================================================================
    // CASO DE PRUEBA 1: CÉSAR sobre "FUNCIONAL" con k=3 + descifrado k=-3
    // =================================================================
    println("\n" + "-" * 70)
    println("CASO 1: CIFRADO CESAR  E(x,k) = (pos(x) + k) mod 26")
    println("-" * 70)

    val cesarCifrado = procesarMensaje("FUNCIONAL", 3, cifradoCesar, "CESAR")
    mostrar("Cifrar \"FUNCIONAL\" con k=3", cesarCifrado)

    // Descifrado: reutilizamos la MISMA higher order function invirtiendo
    // el signo de la clave (k=-3). Aquí sí hace falta invertir el signo.
    val textoCifrado = cesarCifrado.map(_.procesado).getOrElse("")
    val cesarDescifrado = procesarMensaje(textoCifrado, -3, cifradoCesar, "CESAR")
    mostrar("Descifrar el resultado con k=-3 (signo invertido)", cesarDescifrado)

    // Verificación con pattern matching y comparación de strings
    val recuperadoOk = cesarDescifrado.map(_.procesado == "FUNCIONAL").getOrElse(false)
    println(s"    VERIFICACION: ¿se recupero el mensaje original? -> $recuperadoOk")

    // Ejemplo didáctico del enunciado: "AD" con k=1 => "BE"
    val ejemploAd = procesarMensaje("AD", 1, cifradoCesar, "CESAR")
    mostrar("Ejemplo didactico: \"AD\" con k=1 (esperado \"BE\")", ejemploAd)

    // =================================================================
    // CASO DE PRUEBA 2: XOR sobre "scala" con k=5 (doble aplicación)
    // =================================================================
    println("\n" + "-" * 70)
    println("CASO 2: CIFRADO XOR  E(x,k) = ASCII(x) ^ k")
    println("-" * 70)

    val xorCifrado = procesarMensaje("scala", 5, cifradoXOR, "XOR")
    mostrar("Cifrar \"scala\" con k=5 (esperado \"vfdid\")", xorCifrado)

    // Doble aplicación: misma función, MISMA clave k=5 => recupera "scala"
    val textoXor = xorCifrado.map(_.procesado).getOrElse("")
    val xorDoble = procesarMensaje(textoXor, 5, cifradoXOR, "XOR")
    mostrar("Aplicar de nuevo XOR con la MISMA clave k=5", xorDoble)

    val involucionOk = xorDoble.map(_.procesado == "scala").getOrElse(false)
    println(s"    VERIFICACION: ¿XOR es su propia inversa? -> $involucionOk")

    // Demostración carácter a carácter del ejemplo del enunciado
    println("\n    Detalle ASCII de \"scala\" ^ 5:")
    List("s -> 115 ^ 5 = 118 -> v", "c -> 99 ^ 5 = 102 -> f",
         "a -> 97 ^ 5 = 100 -> d", "l -> 108 ^ 5 = 105 -> i",
         "a -> 97 ^ 5 = 100 -> d").foreach(println)

    // =================================================================
    // CASO DE PRUEBA 3: MANEJO DE ERRORES (Try/Success/Failure)
    // =================================================================
    println("\n" + "-" * 70)
    println("CASO 3: MANEJO DE EXCEPCIONES (captura elegante)")
    println("-" * 70)

    // Fallo 1: cadena vacía
    mostrar("Forzar fallo: mensaje vacio",
            procesarMensaje("", 3, cifradoCesar, "CESAR"))

    // Fallo 2: clave excesivamente grande
    mostrar("Forzar fallo: clave excesivamente grande k=99999",
            procesarMensaje("HOLA", 99999, cifradoCesar, "CESAR"))

    // Fallo 3: caracteres no soportados en César (minúsculas/especiales)
    mostrar("Forzar fallo: caracteres no soportados en Cesar \"H0LA!\"",
            procesarMensaje("H0LA!", 3, cifradoCesar, "CESAR"))

    // =================================================================
    // CASO EXTRA: análisis funcional de un mensaje cifrado
    // (foldLeft, scanLeft, maxBy, count, distancia media con .toDouble)
    // =================================================================
    println("\n" + "-" * 70)
    println("CASO EXTRA: ANALISIS FUNCIONAL SOBRE EL MENSAJE CIFRADO")
    println("-" * 70)

    val mensajeBase = "FUNCIONAL"
    val cifradoExtra = transformar(mensajeBase, 3, cifradoCesar)
    println(s"    Mensaje base        : \"$mensajeBase\"")
    println(s"    Cifrado (k=3)       : \"$cifradoExtra\"")
    println(s"    Frecuencias         : ${frecuenciaCaracteres(cifradoExtra)}")
    println(s"    Caracter mas frec.  : ${caracterMasFrecuente(cifradoExtra)}")
    println(s"    Serie ascii acum.   : ${serieAcumuladaAscii(cifradoExtra)}")
    println(s"    Vocales en original : ${mensajeBase.count(c => "AEIOU".contains(c))}")
    println(s"    Distancia media     : ${distanciaMedia(mensajeBase, cifradoExtra)}")

    // Comparativa de algoritmos con una lista de funciones (higher order)
    println("\n    Comparativa de metodos (lista de funciones como datos):")
    val metodos: List[(String, (Char, Int) => Char)] = List(
      ("CESAR", cifradoCesar),
      ("XOR", cifradoXOR)
    )
    metodos.foreach { (nombre, fn) =>
      val res = procesarMensaje("ABC", 5, fn, nombre)
      res.foreach(r => println(s"      $nombre con k=5 sobre \"ABC\" -> \"${r.procesado}\""))
    }

    println("\n" + "=" * 70)
    println("  FIN DE LA DEMOSTRACION")
    println("=" * 70)
}
