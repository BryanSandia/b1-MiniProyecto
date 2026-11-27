import scala.util.{Try, Success, Failure}

// Cifrado Cesar: E(x,k) = (pos(x) + k) mod 26, solo letras mayusculas A-Z
def cifradoCesar(caracter: Char, clave: Int): Try[Char] = {
  if (!caracter.isUpper || caracter < 'A' || caracter > 'Z')
    Failure(new IllegalArgumentException(s"Caracter no soportado en Cesar: '$caracter'"))
  else {
    val pos = (caracter - 'A' + clave % 26 + 26) % 26
    Success(('A' + pos).toChar)
  }
}

// Cifrado XOR: E(x,k) = ASCII(x) ^ k, funciona con cualquier caracter ASCII
def cifradoXOR(caracter: Char, clave: Int): Try[Char] = {
  if (clave < 0 || clave > 127)
    Failure(new IllegalArgumentException(s"Clave invalida para XOR: $clave"))
  else if (caracter > 127)
    Failure(new IllegalArgumentException(s"Caracter no ASCII para XOR: '$caracter'"))
  else
    Success((caracter.toInt ^ clave).toChar)
}

// Funcion de orden superior: recibe la funcion de transformacion como parametro
def procesarMensaje(mensaje: String, clave: Int, funcionCifrado: (Char, Int) => Try[Char]): Try[String] = {
  if (mensaje == null || mensaje.isEmpty)
    Failure(new IllegalArgumentException("El mensaje esta vacio o es nulo"))
  else {
    val resultados = mensaje.map(c => funcionCifrado(c, clave))
    resultados.collectFirst { case Failure(error) => error } match {
      case Some(error) => Failure(error)
      case None => Success(resultados.collect { case Success(caracter) => caracter }.mkString)
    }
  }
}

// === CIFRADO CESAR ===
println("=== CIFRADO CESAR ===")
procesarMensaje("FUNCIONAL", 3, cifradoCesar).foreach(c => {
  println(s"Cifrado (k=3):    $c")
  procesarMensaje(c, -3, cifradoCesar).foreach(d => println(s"Descifrado (k=-3): $d"))
})
procesarMensaje("AD", 1, cifradoCesar).foreach(c => println(s"Ejemplo: $c"))

// === CIFRADO XOR ===
println("\n=== CIFRADO XOR ===")
procesarMensaje("scala", 5, cifradoXOR).foreach(c => {
  println(s"Cifrado (k=5):    $c")
  procesarMensaje(c, 5, cifradoXOR).foreach(d => println(s"Inverso (k=5):  $d"))
})

// === MANEJO DE ERRORES ===
println("\n=== MANEJO DE ERRORES ===")
def comprobarFallo(nombre: String, resultado: Try[String], errorEsperado: String) = resultado match {
  case Failure(error) if error.getMessage.contains(errorEsperado) =>
    println(s"PRUEBA OK: $nombre -> ${error.getMessage}")
  case Failure(error) =>
    throw new AssertionError(s"$nombre produjo un error inesperado: ${error.getMessage}")
  case Success(valor) =>
    throw new AssertionError(s"$nombre debia fallar, pero devolvio: $valor")
}

def comprobarExito(nombre: String, resultado: Try[String], resultadoEsperado: String) = resultado match {
  case Success(valor) if valor == resultadoEsperado =>
    println(s"PRUEBA OK: $nombre -> $valor")
  case Success(valor) =>
    throw new AssertionError(s"$nombre devolvio '$valor' en lugar de '$resultadoEsperado'")
  case Failure(error) =>
    throw new AssertionError(s"$nombre fallo inesperadamente: ${error.getMessage}")
}

comprobarFallo(
  "mensaje vacio",
  procesarMensaje("", 3, cifradoCesar),
  "vacio o es nulo"
)
comprobarExito(
  "clave grande valida para Cesar",
  procesarMensaje("HOLA", 100000, cifradoCesar),
  "LSPE"
)
comprobarFallo(
  "caracter no manejado por Cesar",
  procesarMensaje("HOLA!", 3, cifradoCesar),
  "Caracter no soportado en Cesar"
)
comprobarFallo(
  "clave fuera del rango XOR",
  procesarMensaje("scala", 100000, cifradoXOR),
  "Clave invalida para XOR"
)
comprobarFallo(
  "caracter fuera de ASCII para XOR",
  procesarMensaje("ñ", 5, cifradoXOR),
  "Caracter no ASCII para XOR"
)
