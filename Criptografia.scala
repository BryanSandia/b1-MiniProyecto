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
  else
    Success((caracter.toInt ^ clave).toChar)
}

// Funcion de orden superior: recibe la funcion de transformacion como parametro
def procesarMensaje(mensaje: String, clave: Int, funcionCifrado: (Char, Int) => Try[Char]): Try[String] = {
  if (mensaje == null || mensaje.isEmpty)
    Failure(new IllegalArgumentException("El mensaje esta vacio o es nulo"))
  else if (math.abs(clave) > 9999)
    Failure(new IllegalArgumentException(s"Clave excesivamente grande: $clave"))
  else {
    val resultados = mensaje.map(c => funcionCifrado(c, clave))
    if (resultados.exists(_.isFailure))
      Failure(resultados.find(_.isFailure).get.failed.get)
    else
      Success(resultados.map(_.get).mkString)
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
List(
  procesarMensaje("", 3, cifradoCesar),
  procesarMensaje("HOLA", 100000, cifradoCesar),
  procesarMensaje("H0LA!", 3, cifradoCesar),
  procesarMensaje("scala", 9999, cifradoXOR)
).foreach {
  case Success(r) => println(s"OK: $r")
  case Failure(e) => println(s"Fallo: ${e.getMessage}")
}
