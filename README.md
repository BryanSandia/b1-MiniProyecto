# 🔐 Mini Proyecto Scala — Higher Order Functions en Criptografía

> **Programación Funcional y Reactiva** | Primer Bimestre 2026  
> **Autor:** Bryan Sandia  
> **Fecha de entrega:** Semana 8

---

## 📑 Tabla de Contenidos

1. [Objetivos](#-objetivos)
2. [Descripción del Proyecto](#-descripción-del-proyecto)
3. [Algoritmos Implementados](#-algoritmos-implementados)
4. [Desarrollo del Código](#-desarrollo-del-código)
   - 4.1 [Cifrado César](#41-cifrado-césar)
   - 4.2 [Cifrado XOR](#42-cifrado-xor)
   - 4.3 [Función de Orden Superior `procesarMensaje`](#43-función-de-orden-superior-procesarmensaje)
5. [Casos de Prueba y Resultados](#-casos-de-prueba-y-resultados)
   - 5.1 [Caso César](#51-caso-césar)
   - 5.2 [Caso XOR](#52-caso-xor)
   - 5.3 [Manejo de Errores](#53-manejo-de-errores)
6. [Preguntas de Reflexión](#-preguntas-de-reflexión)
7. [Estructura del Repositorio](#-estructura-del-repositorio)
8. [Cómo Ejecutar](#-cómo-ejecutar)

---

## 🎯 Objetivos

- Aplicar los conocimientos sobre **funciones de orden superior** (Higher Order Functions).
- Utilizar **funciones como parámetros** para definir estrategias de cifrado dinámicas.
- Implementar operaciones de transformación sobre **colecciones inmutables** usando `map`.
- Implementar un **manejo adecuado de excepciones y validación de datos** con `Try/Success/Failure`.

---

## 📖 Descripción del Proyecto

El objetivo es crear un **motor de cifrado** que reciba una función de transformación como parámetro. Se implementan dos métodos de cifrado específicos:

1. **Cifrado César (Desplazamiento):** Mueve cada letra del alfabeto un número fijo de posiciones.
2. **Cifrado XOR (Lógico):** Aplica la operación *Exclusive OR* a nivel de bits entre el código ASCII del carácter y una clave numérica.

El proyecto está desarrollado en **Scala 3**, usando programación funcional pura: sin variables mutables (`var`), sin ciclos imperativos (`for`/`while`), y con manejo de errores mediante `Try/Success/Failure`.

---

## 🔐 Algoritmos Implementados

| Algoritmo | Fórmula | Soporte | Inverso |
|-----------|---------|---------|---------|
| **César** | `E(x, k) = (pos(x) + k) mod 26` | Solo mayúsculas `A-Z` | Se invierte el signo de la clave (`k → -k`) |
| **XOR** | `E(x, k) = ASCII(x) ⊕ k` (operador `^`) | Cualquier carácter ASCII | Se re-aplica la misma clave con el mismo operador |

---

## 💻 Desarrollo del Código

### 4.1 Cifrado César

La función `cifradoCesar` recibe un carácter y una clave, y retorna un `Try[Char]`. Valida que el carácter sea una letra mayúscula `A-Z` y aplica la fórmula de desplazamiento con módulo positivo para soportar claves negativas (descifrado).

```scala
def cifradoCesar(caracter: Char, clave: Int): Try[Char] = {
  if (!caracter.isUpper || caracter < 'A' || caracter > 'Z')
    Failure(new IllegalArgumentException(s"Caracter no soportado en Cesar: '$caracter'"))
  else {
    val pos = (caracter - 'A' + clave % 26 + 26) % 26
    Success(('A' + pos).toChar)
  }
}
