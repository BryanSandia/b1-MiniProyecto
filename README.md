# Mini proyecto Scala: cifrados César y XOR

> **Programación Funcional y Reactiva** · Primer bimestre 2026
> **Autor:** Bryan Sandia

Este documento usa Markdown compatible con GitHub Wiki. El proyecto muestra cómo aplicar cifrados carácter por carácter, pasar una función como argumento y propagar errores con `Try`.

## Tabla de contenidos

1. [Objetivos](#objetivos)
2. [Descripción](#descripción)
3. [Cifrados implementados](#cifrados-implementados)
   - [César](#cifrado-césar)
   - [XOR](#cifrado-xor)
4. [Función de orden superior](#función-de-orden-superior)
5. [Resultados](#resultados)
6. [Manejo de errores y pruebas](#manejo-de-errores-y-pruebas)
7. [Preguntas para reflexionar](#preguntas-para-reflexionar)
8. [Ejecución](#ejecución)

## Objetivos

- Utilizar funciones como parámetros para elegir una estrategia de cifrado.
- Transformar cada carácter de una cadena usando `map`.
- Representar resultados válidos y errores con `Try`, `Success` y `Failure`.
- Comprobar mediante pruebas ejecutables que los datos inválidos no pasan inadvertidos.

## Descripción

`Criptografia.scala` contiene dos funciones que cifran un carácter y una función de orden superior que aplica cualquiera de ellas a un mensaje completo. César solo admite letras mayúsculas de `A` a `Z`; XOR admite caracteres ASCII y claves entre `0` y `127`.

Estos algoritmos se usan con fines didácticos: no son adecuados para proteger información real.

## Cifrados implementados

| Algoritmo | Transformación | Entrada admitida | Descifrado |
|---|---|---|---|
| César | `(posición + clave) mod 26` | Mayúsculas `A-Z`, cualquier clave `Int` | Aplicar la clave con signo contrario |
| XOR | `código ASCII ^ clave` | ASCII (`0-127`), clave `0-127` | Aplicar la misma clave XOR otra vez |

### Cifrado César

`cifradoCesar(caracter, clave)` devuelve `Try[Char]`: `Success` con el carácter transformado, o `Failure` con un `IllegalArgumentException` si el carácter no es una mayúscula inglesa.

Esta es la implementación:

```scala
def cifradoCesar(caracter: Char, clave: Int): Try[Char] = {
  if (!caracter.isUpper || caracter < 'A' || caracter > 'Z')
    Failure(new IllegalArgumentException(s"Caracter no soportado en Cesar: '$caracter'"))
  else {
    val pos = (caracter - 'A' + clave % 26 + 26) % 26
    Success(('A' + pos).toChar)
  }
}
```

Primero comprueba que el carácter sea una mayúscula ASCII de `A` a `Z`. Luego convierte la letra a una posición de `0` a `25`, suma la clave reducida módulo 26 y vuelve a convertir la posición en letra. Si la entrada no es válida, retorna `Failure` en vez de intentar cifrarla.

Por ejemplo, con `A` y clave `3`, la posición `0` pasa a `3`, así que el resultado es `D`. Para el mensaje `HOLA`, cada letra se desplaza tres posiciones:

```scala
procesarMensaje("HOLA", 3, cifradoCesar)   // Success("KROD")
procesarMensaje("KROD", -3, cifradoCesar)  // Success("HOLA")
```

La función reduce cualquier clave `Int` módulo 26. La suma de 26 evita un resto negativo al descifrar con claves negativas, por lo que también se admiten desplazamientos grandes.

Si un mensaje contiene espacios, minúsculas, dígitos o signos de puntuación, César no los convierte silenciosamente: el primer carácter inválido hace que el resultado completo sea `Failure`.

### Cifrado XOR

`cifradoXOR(caracter, clave)` devuelve también `Try[Char]`. Convierte el carácter ASCII a su valor numérico, aplica el operador XOR (`^`) y convierte el resultado de vuelta a carácter.

Esta es la implementación:

```scala
def cifradoXOR(caracter: Char, clave: Int): Try[Char] = {
  if (clave < 0 || clave > 127)
    Failure(new IllegalArgumentException(s"Clave invalida para XOR: $clave"))
  else if (caracter > 127)
    Failure(new IllegalArgumentException(s"Caracter no ASCII para XOR: '$caracter'"))
  else
    Success((caracter.toInt ^ clave).toChar)
}
```

La función verifica primero que la clave esté en el rango permitido, y luego que el carácter sea ASCII. Si ambas validaciones pasan, convierte el carácter a su código numérico, calcula `código ^ clave` y convierte el resultado de nuevo a `Char`. Como ambos valores están entre `0` y `127`, el carácter resultante también queda dentro del rango ASCII.

Ejemplo carácter por carácter: el código ASCII de `A` es `65`; `65 ^ 7` es `70`, que corresponde a `F`.

```scala
procesarMensaje("A", 7, cifradoXOR) // Success("F")
procesarMensaje("F", 7, cifradoXOR) // Success("A")
```

El mensaje `scala`, cifrado con clave `5`, produce `vfdid`. XOR limita tanto el carácter original como la clave a ASCII; algunos resultados válidos pueden ser caracteres de control y no verse como texto imprimible.

## Función de orden superior

`procesarMensaje` es una función de orden superior porque recibe otra función como argumento. Esta es su implementación:

```scala
def procesarMensaje(
    mensaje: String,
    clave: Int,
    funcionCifrado: (Char, Int) => Try[Char]
): Try[String] = {
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
```

El parámetro `funcionCifrado` tiene tipo `(Char, Int) => Try[Char]`: recibe un carácter y una clave, y devuelve el resultado cifrado o un error. Por eso se puede pasar `cifradoCesar` o `cifradoXOR` sin cambiar el código que recorre el mensaje.

La función trabaja así:

1. Valida que el mensaje no sea nulo ni vacío. En caso de error, retorna `Failure` inmediatamente.
2. `mensaje.map(c => funcionCifrado(c, clave))` llama al cifrado para cada carácter y produce una colección de `Try[Char]`.
3. `collectFirst` busca el primer `Failure`; si lo encuentra, lo devuelve como `Failure[String]`.
4. Si no hay fallos, `collect` obtiene los caracteres de los `Success` y `mkString` los une. El resultado final es `Success[String]`.

`procesarMensaje` no fija un rango común para las claves: cada estrategia valida las restricciones que le corresponden. Por ejemplo, César acepta cualquier `Int`, mientras que XOR requiere una clave entre `0` y `127`.

Así se ven los tipos de resultado al usar la función:

```scala
procesarMensaje("HOLA", 3, cifradoCesar) // Success("KROD")
procesarMensaje("HOLA", 100000, cifradoCesar) // Success("LSPE")
procesarMensaje("HOLA!", 3, cifradoCesar) // Failure(IllegalArgumentException(...))
```

`map` ya ha invocado la función de cifrado para todos los caracteres antes de que `collectFirst` encuentre el primer fallo. No se usa `.get` para extraer resultados, así que un `Failure` se propaga como valor y no como una excepción no controlada.

## Resultados

Al ejecutar el archivo se imprimen las transformaciones y el descifrado de los ejemplos:

```text
=== CIFRADO CESAR ===
Cifrado (k=3):    IXQFLRQDO
Descifrado (k=-3): FUNCIONAL
Ejemplo: BE

=== CIFRADO XOR ===
Cifrado (k=5):    vfdid
Inverso (k=5):  scala
```

Además, se ejecutan las pruebas de fallo descritas a continuación. Cada error esperado se imprime con el prefijo `PRUEBA OK`. Si una llamada devuelve éxito cuando debería fallar, o devuelve un error distinto al esperado, se lanza un `AssertionError` y la ejecución termina con fallo.

## Manejo de errores y pruebas

Las funciones no lanzan una excepción para los errores de validación normales: retornan `Failure(new IllegalArgumentException(...))`. `procesarMensaje` propaga el primer fallo como resultado `Failure[String]`, y el código de demostración lo captura con el patrón `case Failure(error)` para mostrar su mensaje. Si todas las validaciones pasan, se recibe `Success(texto)`.

El archivo contiene comprobaciones ejecutables que fuerzan estos errores:

| Caso | Entrada de prueba | Error esperado |
|---|---|---|
| Mensaje vacío | `("", 3, cifradoCesar)` | Mensaje vacío o nulo |
| Carácter especial no admitido por César | `("HOLA!", 3, cifradoCesar)` | Carácter no soportado en César |
| Clave no admitida por XOR | `("scala", 100000, cifradoXOR)` | Clave inválida para XOR |
| Carácter no ASCII para XOR | `("ñ", 5, cifradoXOR)` | Carácter no ASCII para XOR |

También se comprueba que una clave grande (`100000`) funcione con César (`Success("LSPE")`) y sea rechazada por XOR. Así, la clave se valida en el cifrado que conoce sus propias restricciones, no en la función genérica que procesa el mensaje.

Estas pruebas son autocontenidas y usan la biblioteca estándar de Scala, sin framework externo. Ejecutar el script corre tanto los ejemplos válidos como las comprobaciones; no hace falta provocar fallos manualmente.

## Preguntas para reflexionar

### ¿Por qué XOR es su propia función inversa con el mismo operador `^`, mientras que César requiere invertir el signo de la clave?

XOR tiene la propiedad `x ^ k ^ k = x`: aplicar dos veces la misma clave cancela sus bits, porque cada bit igual XOR da `0` y `0` XOR el bit original lo recupera. César suma la clave a la posición del carácter; para deshacer esa suma se debe restar, es decir, aplicar `-k` módulo 26.

### ¿Qué ventajas ofrece `map` sobre un bucle `for` tradicional en este contexto?

`map` expresa directamente la transformación de cada elemento y devuelve una nueva colección, sin administrar índices ni modificar una colección compartida. También permite componer el procesamiento con operaciones funcionales. No es automáticamente más rápido; aquí su ventaja principal es expresar con claridad la transformación carácter a carácter.

### ¿Cómo ayuda el tipado estático a prevenir errores al pasar funciones como argumentos?

El tipo `(Char, Int) => Try[Char]` exige que la función pasada reciba exactamente un carácter y una clave entera, y que devuelva un `Try[Char]`. El compilador puede detectar, antes de ejecutar, funciones con parámetros o resultados incompatibles con lo que `procesarMensaje` necesita.

## Ejecución

Con Scala CLI instalado, desde la raíz del repositorio:

```bash
scala Criptografia.scala
```

El script imprime las demostraciones y ejecuta las pruebas de errores. Si alguna expectativa deja de cumplirse, la ejecución finaliza con un `AssertionError`.
