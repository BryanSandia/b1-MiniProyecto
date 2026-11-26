import scala.util.{Try, Success, Failure}

// Resultado de una operacion de cifrado/descifrado
case class ResultadoCifrado(original: String, procesado: String, algoritmo: String, clave: Int)

// Cifrado Cesar: E(x,k) = (pos(x) + k) mod 26, solo letras mayusculas A-Z
def cifradoCesar(caracter: Char, clave: Int): Char = {
  if (!caracter.isUpper || caracter < 'A' || caracter > 'Z')
    throw new IllegalArgumentException(s"Caracter no soportado en Cesar: '$caracter'")
  val pos = (caracter - 'A' + clave % 26 + 26) % 26 // modulo positivo para claves negativas
  ('A' + pos).toChar
}

// Cifrado XOR: E(x,k) = ASCII(x) ^ k, funciona con cualquier caracter ASCII
def cifradoXOR(caracter: Char, clave: Int): Char = {
  if (clave < 0 || clave > 127)
    throw new IllegalArgumentException(s"Clave invalida para XOR: $clave")
  (caracter.toInt ^ clave).toChar
}

// Funcion de orden superior: recibe la funcion de transformacion como parametro
def procesarMensaje(mensaje: String, clave: Int, funcionCifrado: (Char, Int) => Char, algoritmo: String = "generico"): Try[ResultadoCifrado] = Try {
  if (mensaje == null || mensaje.isEmpty)
    throw new IllegalArgumentException("El mensaje esta vacio o es nulo")
  if (math.abs(clave) > 9999)
    throw new IllegalArgumentException(s"Clave excesivamente grande: $clave")
  val procesado = mensaje.map(c => funcionCifrado(c, clave)).mkString
  ResultadoCifrado(mensaje, procesado, algoritmo, clave)
}

@main def ejecutar(): Unit = {

  // Caso 1: Cesar - cifrar "FUNCIONAL" con k=3 y descifrar con k=-3
  println("=== CIFRADO CESAR ===")
  procesarMensaje("FUNCIONAL", 3, cifradoCesar, "Cesar") match
    case Success(r) =>
      println(s"Cifrado:    ${r.original} con k=${r.clave} -> ${r.procesado}")
      procesarMensaje(r.procesado, -3, cifradoCesar, "Cesar") match
        case Success(d) => println(s"Descifrado: ${d.original} con k=${d.clave} -> ${d.procesado}")
        case Failure(e) => println(s"Fallo al descifrar: ${e.getMessage}")
    case Failure(e) => println(s"Fallo: ${e.getMessage}")

  // Ejemplo didactico: "AD" con k=1 debe dar "BE"
  procesarMensaje("AD", 1, cifradoCesar, "Cesar") match
    case Success(r) => println(s"Ejemplo:    ${r.original} con k=${r.clave} -> ${r.procesado}")
    case Failure(e) => println(s"Fallo: ${e.getMessage}")

  // Caso 2: XOR - cifrar "scala" con k=5 (debe dar "vfdid") y re-aplicar para recuperar
  println("\n=== CIFRADO XOR ===")
  procesarMensaje("scala", 5, cifradoXOR, "XOR") match
    case Success(r) =>
      println(s"Cifrado:    ${r.original} con k=${r.clave} -> ${r.procesado}")
      procesarMensaje(r.procesado, 5, cifradoXOR, "XOR") match
        case Success(d) => println(s"Inverso:    ${d.original} con k=${d.clave} -> ${d.procesado}")
        case Failure(e) => println(s"Fallo: ${e.getMessage}")
    case Failure(e) => println(s"Fallo: ${e.getMessage}")

  // Caso 3: Manejo de errores con Try/Success/Failure
  println("\n=== MANEJO DE ERRORES ===")
  val errores = List(
    procesarMensaje("", 3, cifradoCesar, "Cesar"),          // cadena vacia
    procesarMensaje("HOLA", 100000, cifradoCesar, "Cesar"), // clave excesiva
    procesarMensaje("H0LA!", 3, cifradoCesar, "Cesar"),     // caracteres no soportados
    procesarMensaje("scala", 9999, cifradoXOR, "XOR")       // clave fuera de rango XOR
  )
  errores.foreach {
    case Success(r) => println(s"Inesperado OK: ${r.procesado}")
    case Failure(e) => println(s"Fallo capturado: ${e.getMessage}")
  }
}
