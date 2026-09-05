# Post-contenido — Unidad 2: Patrones Creacionales

## Descripción
Repositorio del post-contenido de la Unidad 2 de Patrones de Diseño de Software —
Sexto Semestre. Un único proyecto Maven (`exportador-reportes/`) que resuelve la
exportación de reportes académicos en múltiples formatos (Parte 1) y se extiende
con configuración compleja y evaluación de necesidad de Singleton (Parte 2).

## Cómo ejecutar

cd exportador-reportes
mvn compile
mvn exec:java "-Dexec.mainClass=com.patrones.u2.Main"


## Decisiones de diseño

#Preguntas Diagnosticas

1. ¿Un solo producto que varía por formato, o una familia que debe mantenerse coherente?
Acá hay dos productos relacionados por exportación: el cuerpo (ReportBody) y el encabezado/pie (ReportHeaderFooter). El enunciado es explícito: no se puede mezclar cuerpo PDF con encabezado Excel. Eso es una familia de productos que debe permanecer consistente → apunta a Abstract Factory, no a Factory Method (que resuelve la creación de un único producto).

2. ¿Agregar CSV implica una implementación nueva o una familia nueva completa?
Al agregar CSV necesitarás CsvReportBody + CsvHeaderFooter + CsvReportFactory — una familia completa nueva, no solo una clase suelta. Refuerza Abstract Factory.

3. ¿El riesgo es "clase equivocada" o "mezcla de familias"?
El riesgo real que describe el problema es justamente mezclar piezas de formatos distintos (cuerpo de un formato + encabezado de otro), que es exactamente lo que Abstract Factory previene al obligar a crear ambos productos desde la misma fábrica concreta.

Conclusión: Abstract Factory. Se descarta Factory Method porque ese patrón resuelve bien la creación de un solo producto con variantes (por ejemplo, si solo existiera ReportBody sin necesidad de mantenerlo coherente con nada más), pero aquí necesitamos garantizar que dos productos relacionados salgan siempre de la misma familia — eso es precisamente lo que Factory Method no controla por sí solo.

### Decisión 1 — Factory Method vs. Abstract Factory (Parte 1)

**Patrón elegido:** Abstract Factory

**Justificación:** El problema no crea un único producto que varía por formato,
sino dos productos relacionados (el cuerpo del reporte y el encabezado/pie de
página) que deben mantenerse coherentes entre sí dentro de un mismo formato: no
es válido combinar un cuerpo Excel con un encabezado PDF. Al agregar el futuro
formato CSV, no basta con agregar una única implementación nueva; hay que agregar
una familia completa (cuerpo + encabezado/pie) que sea consistente entre sí. El
riesgo real del problema no es "se instanció la clase equivocada" de forma
aislada, sino que se mezclen piezas de familias distintas y el documento quede
inconsistente — exactamente lo que Abstract Factory previene al forzar que ambos
productos se creen desde la misma fábrica concreta (`PdfReportFactory`,
`ExcelReportFactory`, `HtmlReportFactory`). Se descartó Factory Method porque ese
patrón resuelve bien la creación de un solo producto con variantes, pero no
ofrece ningún mecanismo para garantizar que dos productos relacionados salgan
siempre de la misma familia.

### Decisión 2 — Mecanismo de extensibilidad de formatos (Parte 1)

**Opción elegida:** Registro dinámico con `Map<String, Supplier<ReportFormatFactory>>`

**Justificación:** Un switch o cadena de if/else sobre el string de formato
obligaría a modificar ese mismo método cada vez que se agregue un formato nuevo
(como el CSV planeado), violando el principio Abierto/Cerrado (OCP). Con
`ReportFactoryRegistry`, agregar CSV en el futuro solo requiere llamar a
`ReportFactoryRegistry.register("csv", CsvReportFactory::new)` desde fuera de la
clase, sin modificar `ReportFactoryRegistry` ni ningún código existente.

### Decisión 3 — Builder vs. constructor telescópico vs. setters (Parte 2)

**Opción elegida:** Builder (clase interna `ExportConfig.Builder`)

**Justificación:** `ExportConfig` tiene 1 parámetro obligatorio (`format`) y 8
opcionales. Un constructor con los 9 parámetros obligaría al cliente a recordar
el orden exacto de los argumentos, con varios del mismo tipo (`String`,
`boolean`), lo que permite invertirlos sin que el compilador lo detecte.
Constructores sobrecargados por cada combinación común tampoco escalan: con 8
parámetros opcionales el número de combinaciones razonables crece rápido y la
clase terminaría con demasiados constructores casi idénticos. Una clase mutable
con setters sueltos tampoco resuelve el problema, porque el objeto puede quedar
en un estado a medio configurar y no hay un punto único donde validar que la
combinación de valores sea consistente (por ejemplo, pedir compresión sin indicar
dónde guardar el archivo). El Builder resuelve las tres limitaciones: expone
métodos encadenables legibles por nombre, evita construir combinaciones inválidas
mediante un único método `build()`, y centraliza ahí la validación de consistencia
antes de que exista un objeto `ExportConfig` a medio configurar.

### Decisión 4 — ¿ReportFactoryRegistry necesita ser Singleton? (Parte 2)

**Conclusión:** NO conviene convertirlo en Singleton clásico

**Justificación:** Aplicando los criterios de la Guía Teórica (Secciones 2.1, 2.4
y 7.3):

- **Identidad de objeto:** nada en este proyecto necesita pasar el registro como
  objeto (por ejemplo, para mockearlo en pruebas o inyectarlo por constructor);
  basta con invocar sus métodos estáticos directamente.
- **Inicialización costosa:** crear el registro no implica trabajo costoso
  (leer archivos, abrir conexiones); es un `Map` con tres entradas llenado en un
  bloque `static`, sin costo relevante que se beneficie de inicialización lazy.
- **Fuente única de verdad:** el `Map` estático ya garantiza una única instancia
  compartida en toda la JVM sin necesidad de la maquinaria de Singleton
  (constructor privado con guardas, método `getInstance()`, sincronización).
- **Escenarios futuros razonables:** no existe en este proyecto un escenario
  concreto que requiera múltiples registros independientes; de existir (por
  ejemplo, una plataforma multi-institución), Singleton dejaría de ser apropiado
  por la razón opuesta: se necesitarían múltiples instancias, no una sola.

Convertirlo en Singleton agregaría ceremonia sin resolver ningún problema real
—tal como advierte la Guía Teórica sobre clases utilitarias con solo miembros
estáticos.

## Herramientas utilizadas

- Java 17
- Apache Maven
- VS Code
- Git
- GitHub

## Conclusiones

Este post-contenido mostró que elegir un patrón creacional no es un ejercicio de
memorización de estructuras, sino de analizar si el problema exige una familia de
productos coherentes entre sí (Abstract Factory) o un único producto con
variantes (Factory Method). También evidenció que un mecanismo de registro
dinámico basado en `Map` y `Supplier` cumple el principio Abierto/Cerrado mejor
que un switch, sin necesidad de tocar código existente al crecer. Con Builder
quedó claro cómo centralizar la validación de un objeto con muchos parámetros
opcionales en un único punto (`build()`), evitando estados inconsistentes a medio
construir. Finalmente, evaluar la necesidad real de Singleton para
`ReportFactoryRegistry` enseñó que un patrón no debe aplicarse "por costumbre":
un campo estático puede ya resolver la fuente única de verdad sin necesitar la
ceremonia adicional de Singleton.
