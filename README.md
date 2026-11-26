# 🔐 Mini Proyecto Scala — Higher Order Functions en Criptografía

Motor de cifrado construido con **programación funcional pura** en Scala. El
corazón del proyecto es una **función de orden superior** (`procesarMensaje`)
que recibe la estrategia de cifrado como parámetro, demostrando el poder de
tratar funciones como valores de primera clase.

## 📌 Descripción del proyecto

El proyecto implementa dos algoritmos clásicos de cifrado:

| Algoritmo | Fórmula | Alfabeto soportado | Inverso |
|-----------|---------|--------------------|---------|
| **César (desplazamiento)** | `E(x, k) = (pos(x) + k) mod 26` | Solo letras mayúsculas `A-Z` | Se invierte el signo de la clave (`k → -k`) |
| **XOR (lógico)** | `E(x, k) = ASCII(x) ⊕ k` (operador `^`) | Cualquier carácter imprimible | Se reutiliza la **misma** clave y el **mismo** operador |

### Estructura del código (`Criptografia.scala`)

- **`case class ResultadoCifrado(original, procesado, algoritmo, clave)`** — modelo
  de datos con semántica e inmutabilidad.
- **Validaciones puras con `Either[String, A]`**:
  - `validarMensaje` → captura cadenas nulas o vacías (`Left`).
  - `validarClave` → captura claves fuera de rango `|k| > 1000` (`Left`).
  - `sonLetrasMayusculas` (`forall`) y `caracteresNoSoportados` (`count`).
- **`cifradoCesar(caracter: Char, clave: Int): Char`** — convierte el carácter a
  índice (`A=0 … Z=25`), aplica `(pos + k) mod 26` con módulo normalizado a
  positivo y vuelve a `Char`. Usa `Try/Success/Failure` internamente.
- **`cifradoXOR(caracter: Char, clave: Int): Char`** — aplica `ASCII(x) ^ k`.
- **⭐ `procesarMensaje(mensaje, clave, funcionCifrado, nombreAlgoritmo): Try[ResultadoCifrado]`**
  — función de **orden superior**: recibe `funcionCifrado: (Char, Int) => Char`
  como parámetro y la aplica a cada carácter con `map`. Todo el manejo de
  errores se propaga elegantemente como `Failure`.
- **Análisis funcional extra**: `frecuenciaCaracteres` (`foldLeft` sobre `Map`
  inmutable), `caracterMasFrecuente` (`maxBy`), `serieAcumuladaAscii`
  (`scanLeft`), `distanciaMedia` (`foldLeft` + `.toDouble`),
  `esInvolucion` (`forall`), `caracterMenosDesplazado` (`minBy`).

> ✔ Sin `var`, sin ciclos `for`/`while` imperativos, funciones puras
> (mismo input ⇒ mismo output, sin efectos secundarios).

## 🛠️ Compilación y ejecución

Requiere JDK 8+ y Scala 3 (Scala 2.13 también funciona; los únicos ajustes
serían convertir la sintaxis de indentación del `match` a llaves si se usa
una versión antigua).

```bash
# Compilar
scalac Criptografia.scala

# Ejecutar la demo completa (todos los casos de prueba)
scala CriptoApp
```

Alternativa con sbt (opcional): copia el archivo a `src/main/scala/` y ejecuta
`sbt run`.

## ✅ Resultados de los casos de prueba (verificados)

### Caso 1 — César: `"FUNCIONAL"` con `k = 3`

```text
Original  : "FUNCIONAL"
Resultado : "IXQFLRQDO"
Descifrado con k = -3 (signo invertido) -> "FUNCIONAL"
VERIFICACION: ¿se recupero el mensaje original? -> true
```

Ejemplo didáctico del enunciado: `"AD"` con `k = 1` → `'A'(0): (0+1) mod 26 = 1 → 'B'`,
`'D'(3): (3+1) mod 26 = 4 → 'E'` ⇒ resultado **"BE"** ✔

### Caso 2 — XOR: `"scala"` con `k = 5`

```text
Original  : "scala"
Resultado : "vfdid"        (115^5=118 'v', 99^5=102 'f', 97^5=100 'd',
                             108^5=105 'i', 97^5=100 'd')
Segunda aplicacion con la MISMA clave k = 5 -> "scala"
VERIFICACION: ¿XOR es su propia inversa? -> true
```

### Caso 3 — Manejo de excepciones (`Try/Success/Failure`)

```text
>>> Forzar fallo: mensaje vacio
    FAILURE -> IllegalStateException: ERROR: el mensaje esta vacio

>>> Forzar fallo: clave excesivamente grande k=99999
    FAILURE -> IllegalStateException: ERROR: clave 99999 fuera de rango (|k| debe ser <= 1000)

>>> Forzar fallo: caracteres no soportados en Cesar "H0LA!"
    FAILURE -> IllegalArgumentException: Cesar no soporta caracteres
               especiales/minusculas (2 caracter(es) invalidos en "H0LA!")
```

Ningún fallo rompe el programa: todos se capturan como `Failure` y se imprimen
con pattern matching.

### Caso extra — análisis funcional

```text
Cifrado (k=3)       : "IXQFLRQDO"
Frecuencias         : HashMap(X->1, F->1, I->1, R->1, O->1, D->1, Q->2, L->1)
Caracter mas frec.  : Some((Q,2))
Serie ascii acum.   : List(0, 73, 161, 242, 312, 388, 470, 551, 619, 698)
Vocales en original : 4          (count + lambda)
Distancia media     : 3.0         (foldLeft + .toDouble)
Comparativa (lista de funciones como datos):
  CESAR con k=5 sobre "ABC" -> "FGH"
  XOR   con k=5 sobre "ABC" -> "DGF"
```

## 💭 Preguntas de reflexión

### 1. ¿Por qué XOR es su propia función inversa con el mismo operador `^`, mientras que César requiere invertir el signo de la clave?

Porque el XOR es una **operación involutiva** por construcción algebraica: para
cualquier bit `a` y clave `k` se cumple que `(a ⊕ k) ⊕ k = a ⊕ (k ⊕ k) = a ⊕ 0 = a`
(el XOR es asociativo y todo valor XOR consigo mismo da 0). Cifrar y descifrar
son **literalmente la misma operación** con la misma clave; no existe un
"descifrado" distinto. En cambio, César es una **traslación aritmética** en el
grupo `Z₂₆`: `E(x,k) = (x+k) mod 26`. Su inverso matemático es la resta
`D(y,k) = (y−k) mod 26`; para reutilizar la misma función debemos alimentar la
clave negada (`k → −k`), es decir, usar el **elemento inverso aditivo** de la
clave. En resumen: XOR se invierte a sí mismo (involución / auto-inversidad);
César necesita el inverso aditivo de su parámetro.

### 2. ¿Qué ventajas ofrece `map` sobre un bucle `for` tradicional en este contexto?

- **Transparencia referencial y pureza**: `mensaje.map(c => f(c, k))` no muta
  estado; no necesita un acumulador mutable (`var resultado = ""`) ni índices,
  eliminando errores típicos (off-by-one, mutación compartida).
- **La función de transformación es un parámetro**: al expresar el algoritmo
  como `map`, la estrategia de cifrado `(Char, Int) => Char` puede inyectarse
  como argumento — algo natural con higher order functions y engorroso dentro
  de un `for` imperativo. Cambiar de César a XOR es cambiar un argumento, no
  reescribir el ciclo.
- **Composibilidad**: el resultado es otro `String`/colección, encadenable con
  `filter`, `foldLeft`, `exists`, etc., y convertible fácilmente a paralelismo
  (`.par`) sin reestructurar código.
- **Manejo de errores integrado**: las excepciones de cualquier carácter quedan
  capturadas por un único `Try` que envuelve toda la expresión, en lugar de
  requerir `try/catch` dentro del cuerpo mutable del bucle.
- **Declaratividad**: el código dice *qué* se hace (transformar cada carácter),
  no *cómo* recorrerlo, lo que lo hace más legible y verificable.

### 3. ¿Cómo ayuda el tipado estático a prevenir errores al pasar funciones como argumentos?

El tipo `(Char, Int) => Char` forma parte de la firma de `procesarMensaje`, así
que el compilador verifica **en tiempo de compilación** que toda función pasada:

1. Reciba exactamente un `Char` y un `Int` (no se puede pasar por error una
   `(String, Int) => String` mal tipada, ni intercambiar el orden de
   parámetros).
2. Devuelva un `Char` — si olvidamos el `.toChar` tras `^` y devolvemos `Int`,
   el compilador lo rechaza inmediatamente (error real detectado durante el
   desarrollo de este proyecto).
3. Sea total respecto al contrato: combinado con `Try[ResultadoCifrado]` y
   `Either[String, A]`, los efectos de error son **visibles en el tipo**,
   obligando al llamador a hacer pattern matching sobre `Success/Failure` o
   `Left/Right` en lugar de confiar en excepciones invisibles en tiempo de
   ejecución.

En lenguajes dinámicos estos errores solo aparecen en runtime (o silenciosamente
producen salida corrupta); aquí son imposibles de compilar, lo que convierte el
tipado estático en la primera línea de defensa del motor de cifrado.

## 📁 Archivos del repositorio

```text
.
├── Criptografia.scala   # Motor de cifrado + casos de prueba (object CriptoApp)
└── README.md            # Este documento
```

---
Proyecto académico — Scala funcional puro (Higher Order Functions).
