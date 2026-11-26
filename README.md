# 🔐 Mini Proyecto Scala — Higher Order Functions en Criptografía

Motor de cifrado en **programación funcional pura** (Scala 3). El corazón del
proyecto es `procesarMensaje`, una **función de orden superior** que recibe la
estrategia de cifrado como parámetro `(Char, Int) => Char` y la aplica con `map`.

## Algoritmos

| Algoritmo | Fórmula | Soporte | Inverso |
|-----------|---------|---------|---------|
| **César** | `E(x, k) = (pos(x) + k) mod 26` | Solo mayúsculas `A-Z` | Se invierte el signo de la clave (`k → -k`) |
| **XOR** | `E(x, k) = ASCII(x) ⊕ k` (operador `^`) | Cualquier carácter ASCII | Se re-aplica la misma clave con el mismo operador |

Sin `var`, sin ciclos imperativos, funciones puras (mismo input ⇒ mismo output).

## Compilación y ejecución

```bash
scalac Criptografia.scala
scala ejecutar   # punto de entrada @main
```

## Resultados de los casos de prueba (verificados)

### César
```text
Cifrado:    FUNCIONAL con k=3 -> IXQFLRQDO
Descifrado: IXQFLRQDO con k=-3 -> FUNCIONAL
Ejemplo:    AD con k=1 -> BE
```

### XOR
```text
Cifrado:    scala con k=5 -> vfdid
Inverso:    vfdid con k=5 -> scala
```
(s'115^5=118 'v', c'99^5=102 'f', a'97^5=100 'd', l'108^5=105 'i', a'97^5=100 'd')

### Manejo de errores (Try/Success/Failure)
```text
Fallo capturado: El mensaje esta vacio o es nulo            (cadena vacia)
Fallo capturado: Clave excesivamente grande: 100000         (clave invalida)
Fallo capturado: Caracter no soportado en Cesar: '0'        ("H0LA!")
Fallo capturado: Clave invalida para XOR: 9999              (fuera de rango ASCII)
```
Ningún fallo rompe el programa: todos se capturan como `Failure` y se imprimen
con pattern matching.

## Preguntas de reflexión

### 1. ¿Por qué XOR es su propia inversa y César no?

XOR es una operación **involutiva**: `(a ⊕ k) ⊕ k = a ⊕ (k ⊕ k) = a ⊕ 0 = a`,
porque todo bit XOR consigo mismo da 0. Cifrar y descifrar son literalmente la
misma operación con la misma clave. César es una traslación aritmética en `Z₂₆`:
`E(x,k) = (x+k) mod 26`, cuyo inverso es la resta `(y−k) mod 26`; para reutilizar
la misma función hay que alimentar el **inverso aditivo** de la clave (`k → −k`).

### 2. ¿Qué ventajas ofrece `map` sobre un bucle `for` tradicional?

- **Pureza**: no requiere acumuladores mutables (`var`) ni índices; desaparecen
  los errores off-by-one.
- **Inyección de la estrategia**: al ser `map`, la función de cifrado
  `(Char, Int) => Char` pasa como argumento — cambiar de César a XOR es cambiar
  un parámetro, no reescribir un ciclo.
- **Composibilidad**: el resultado encadena con `filter`, `foldLeft`, etc.
- **Errores centralizados**: un solo `Try` envuelve toda la transformación.

### 3. ¿Cómo ayuda el tipado estático al pasar funciones como argumentos?

La firma `funcionCifrado: (Char, Int) => Char` obliga al compilador a verificar
que toda función pasada reciba `Char`+`Int` y devuelva `Char` (si olvidamos el
`.toChar` tras `^` y devolvemos `Int`, no compila). Combinado con
`Try[ResultadoCifrado]`, el error es visible en el tipo y el llamador debe hacer
matching sobre `Success/Failure`. En lenguajes dinámicos esos errores solo
aparecerían en runtime.

## Archivos

```text
.
├── Criptografia.scala   # case class + funciones + @main con casos de prueba
└── README.md
```
