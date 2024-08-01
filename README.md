```
                        ███████╗██╗    ██╗███████╗███████╗████████╗                        
                        ██╔════╝██║    ██║██╔════╝██╔════╝╚══██╔══╝                        
                        ███████╗██║ █╗ ██║█████╗  █████╗     ██║                           
                        ╚════██║██║███╗██║██╔══╝  ██╔══╝     ██║                           
                        ███████║╚███╔███╔╝███████╗███████╗   ██║                           
                        ╚══════╝ ╚══╝╚══╝ ╚══════╝╚══════╝   ╚═╝                           
████████╗██╗ ██████╗██╗  ██╗███████╗████████╗██████╗ ███████╗███████╗██╗ ██████╗ ███╗   ██╗
╚══██╔══╝██║██╔════╝██║ ██╔╝██╔════╝╚══██╔══╝██╔══██╗██╔════╝██╔════╝██║██╔════╝ ████╗  ██║
   ██║   ██║██║     █████╔╝ █████╗     ██║   ██║  ██║█████╗  ███████╗██║██║  ███╗██╔██╗ ██║
   ██║   ██║██║     ██╔═██╗ ██╔══╝     ██║   ██║  ██║██╔══╝  ╚════██║██║██║   ██║██║╚██╗██║
   ██║   ██║╚██████╗██║  ██╗███████╗   ██║   ██████╔╝███████╗███████║██║╚██████╔╝██║ ╚████║
   ╚═╝   ╚═╝ ╚═════╝╚═╝  ╚═╝╚══════╝   ╚═╝   ╚═════╝ ╚══════╝╚══════╝╚═╝ ╚═════╝ ╚═╝  ╚═══╝
```

[![Maven Central](https://img.shields.io/maven-central/v/pe.puyu/SweetTicketDesign.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/pe.puyu/SweetTicketDesign)<br>
SweetTicketDesign implementa interfaces y especificaciones para el diseño tickets responsives en impresoras.
Trae incluido 
- EscPosPrinter: Una implementación de SweetPrinter para renderizar los diseños en ticketeras termicas
  usando comandos escpos usando la libreria [escpos coffee](https://github.com/anastaciocintra/escpos-coffee)
- GsonPrinterObjectBuilder: Una implementación de SweetPrinterObjectBuilder para construción de componentes de impresión 
  a travéz archivos json.
- DefaultComponentsProvider: Un proveedor de componentes de impresión con valores aceptables por defecto.

1. [Empezando](#empezando)
2. [Uso basico](#uso-basico)
[Considerar logo y QR en el diseño de tickets (boleta y facturas)](#codigo-qr-ticket)
[Extra: Utilidad de impresión](#referencia-puka)

<h2 id="empezando">✨Empezando</h2>

SweetTicketDesign esta disponible como dependencia en Maven Central.
Agrega lo siguiente a tu pom.xml

```xml

<dependency>
  <groupId>pe.puyu</groupId>
  <artifactId>SweetTicketDesign</artifactId>
  <version><!--Aqui va la version, ejm: 0.1.0--></version>
</dependency>
```

> Ultima
> versión: [![Maven Central](https://img.shields.io/maven-central/v/pe.puyu/SweetTicketDesign.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/pe.puyu/SweetTicketDesign)

<h2 id="uso-basico">📚 Uso Basico</h2>

SweetTicketDesign ofrece dos clases de diseño, SweetTicketDesign (apartir de v 0.1.0) para diseño de tickets de punto de venta (POS)
y SweetTableDesign (apartir de v 1.0.0) para diseño de tablas responsive ideal para reportes. Ambas clases tienen el mismo comportamiento de instanciación. 

1. Ambas clases tiene constructores para aceptar un JsonObject de [gson](https://github.com/google/gson) o un String (formato json).
2. El diseño sera devuelto en bytes de comandos escpos con la
   llamada al metodo getBytes().
3. Los bytes escpos ahora pueden ser escritos en cualquier objeto que sea
   instancia de clases que hereden de la clase abstracta OutputStream.
   Por ejemplo socket.getOutputStream(), PrinterOutputStream, SerialStream,
   PipedOuputStream, etc.<br>
   > Nota: Los bytes al ser comandos escpos no pueden ser enviados directamente a
   la consola ya que no seran interpretados correctamente, al ser comandos
   escpos tienen que ser enviados a un OutputStream asociado a una ticketera con
   soporte de comandos EscPos.

<h3> Ejemplo</h3>

```java
import com.github.anastaciocintra.output.TcpIpOutputStream;
import pe.puyu.SweetTicketDesign.domain.SweetTicketDesign;

import java.io.OutputStream;

public class Main {
	public static void main(String[] args) {
		try (OutputStream outputStream = new TcpIpOutputStream("192.168.1.100", 9100)) {
			// Diseño de tickets desde version 0.1.0
			String jsonTicket = anyService.getTicket();
			var designTicket = new SweetTicketDesign(jsonTicket);

			outputStream.write(designTicket.getBytes());

			// Diseño de tablas apartir de la versión 1.0.0
			String jsonReport = anyService.getReport();
			var designReport = new SweetTableDesign(jsonReport);

			outputStream.write(designReport.getBytes());

		}
	}
}
```

<h2 id="propiedades-disenio">🛠️Propiedades de diseño</h2>
Se puede personalizar 4 caracteristicas o propiedades de impresión. Adicionalmente a ello existen otras 5 propiedades
mas propias para el diseño de tickets.
Estas propiedades se pueden indicar en la propiedad "printer.properties" del json. 

- [Ver ejemplos json para tickets](#modelos-ticket-soportados)  
- [Ver ejemplos json para tablas](docs/tables.md#ejemplos-de-diseño-para-tablas)

| Propiedad            |Tipo de diseño             | Tipo    | Por defecto | Descripción                                                                                                                                                                                                                                         |
|----------------------|---------------------------|---------|-------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| width                |General(tickets y tablas)  | int     | 42          | Número de caracteres por linea, jugar con este valor segun el tamaño del ticket, por defecto para ticketeras de 80mm, 42 es la valor  deseado.                                                                                                      |
| backgroundInverted   |General(tickets y tablas)  | boolean | true        | Algunas ticketeras antiguas no soportan color de fondo invertido (oscuro) de ser el caso, esta propiedad debe configurarse con el valor false, por defecto es true.                                                                                                                         |
| charCodeTable        |General(tickets y tablas)  | string  | WPC1252     | El tipo de codificación de caracteres según escpos coffee, [enum CharCodeTable, escpos coffee](https://github.com/anastaciocintra/escpos-coffee/blob/master/src/main/java/com/github/anastaciocintra/escpos/EscPos.java).                           |
| textNormalize        |General(tickets y tablas)  | boolean | false       | Ciertas ticketeras no soportan caracteres como: 'áéíóú´d'. Si se establece en true entonces los textos se normalizan  a caracteres basicos. ejm: á -> a.                        |
| fontSizeCommand      |Solo tickets               | int     | 2           | Representa el tamaño de fuente que tendran las comandas, por cuestiones de enfasis en los pedidos el valor por defecto es 2, pero se puede establecer en 1.                                                                                         |
| nativeQR             |Solo tickets               | boolean | false       | Por defecto el qr es generado como imagen y tratado como tal, pero tambien se puede establecer a true para que escpos coffee sea quien genere el qr. Se recomienda el valor por defecto ya que no todas la ticketeras soportan qr nativo de escpos. |
| blankLinesAfterItems |Solo tickets               | int     | 0           | Número de lineas en blanco despues de imprimir los items, por defecto 0. Util en proformas.                                                                                                                                                         |
| showUnitPrice        |Solo tickets               | boolean | false       | Indica si se debe mostrar el precio unitario en la tabla items, y cada item debe tener una propiedad "unitPrice".   Por defecto false.                                                                                                              |
| showProductionArea   |Solo tickets               | boolean | false       | Afecta al diseño de las comandas, indica si se quiere mostrar el area de producción. Por defecto false.                                                                                                                                             |

<h2 id="modelos-ticket-soportados">🔎 Modelos de tickets soportados</h2> 

SweetTicketDesign tiene varios modelos de tickets establecidos: boletas, facturas
extras, encomienda, delivery, comandas para restaurante, extra para restaurante y precuentas.
El modelo que se diseñara dependera del objeto json y su propiedad type.

<h3 id="estructura-general">Estructura general</h3>

Un objeto json ticket puede estar compuesta por varias propiedades, la mayoria de las propiedades
son opcionales, Las propiedades que se tomaran en cuenta depende del
tipo de ticket a diseñar (type). [ver ejemplos de json validos](#ejemplos-de-formato-json-para-el-diseño-de-tickets).

#### Interfaz del ticket

```typescript
interface Ticket {
    type: string; // invoice, note, command, precount, extra 
    times: int; // numero de veces a diseñar del ticket
    printer: {
        properties: {
            width: int // Opcional, por defecto 42
            backgroundInverted: boolean;// Opcional, por defecto true
            charCodeTable: string;// Opcional, por defecto WP1252
						textNormalize: boolean; // opcional por defecto false
            fontSizeCommand: int; // Opcional, por defecto 2
            nativeQR: boolean;// Opcional, por defecto false
            blankLinesAfterItems: int; // opcional, por defecto 0
						showUnitPrice: boolean; // opcional, por defecto false
						showProductionArea: boolean; // opcional por defecto false
        }
    }
    data: {
        business: { // opcional
            comercialDescription: { //opcional
                type: "text" | "img";// valores permitidos text y img
                value: string;
            }
            description: string;
            additional: string[];// opcional 
            document: string;// opcional
            documentNumber: string//opcional
        }
        titleExtra: { //opcional, solo para extras
            title: string;
            subtitle: string;
        }
        productionArea: string // opcional, obligatorio en comandas;
        textBackgroundInverted: string // opcional,obligatorio en comandas anulacion ;
        document: { //opcional
            description: string;
            identifier: string;
        }
        customer: string[];
        additional: string[]; //Array de strings, opcional
        items: [  // Array de objetos opcional
            {
                quantity: int, // opcional
                description: string,
                totalPrice: decimal,
            }, //...
        ]
        amounts: {};// Objeto generico ver ejemplos, opcional
        additionalFooter: string[] // opcional;
        finalMessage: string | string[] // opcional;
        stringQR: string;// opcional
        metadata: { //opcional
            logoPath: string; //opcional   
        }
    }
}
```

> Nota: Lo anterior solo es una especificación de que propiedades tendria
> que contener los objetos json, ver [ejemplos](#ejemplos-disenio-ticket) a continuación.

<h3 id="ejemplos-disenio-ticket">Ejemplos, de formato json para el diseño de tickets.</h3>

1. [Boleta o factura](docs/printing-models.md#1-modelo-para-boleta-o-factura)
2. [Extras](docs/printing-models.md#2-modelo-para-extras)
3. [Encomienda](docs/printing-models.md#3-modelo-para-encomienda)
4. [Comanda restaurante](docs/printing-models.md#4-comanda-restaurante)
5. [Extras o delivery restaurante](docs/printing-models.md#5-modelo-de-extra-o-delivery-restaurante)
6. [Precuenta restaurante](docs/printing-models.md#6-modelo-precuenta-restaurante)
7. [Notas de venta](docs/printing-models.md#7-notas-de-venta)

<h3 id="codigo-qr-ticket">📁 Considerar logo y/o código QR en el diseño de boletas y facturas.</h3>

```javascript
{
    type: "invoice"
    //... las demas propiedades van aqui
    data: {
        business: {
            comercialDescription: {
                type: "img"; // obligatorio para el logo
            }
        }
       //... las demas propiedaes van aqui
       stringQR:"20450523381|01|F001|00000006|0|9.00|30/09/2019|6|sdfsdfsdf|"
    }
    metadata: {
        logoPath: "C:\\Imagenes\\logo.png"
    }
}
```

<h2 id="disenio-tablas">🚀 Diseño de tablas, a partir de la versión 1.0.0 </h2>

Con SweetTableDesign se puede diseñar distintos tipos de tablas con diferentes diseños y personalizaciones.
[Ver ejemplos de formato json](docs/tables.md#ejemplo-1) que sirven como punto de partida. Sin embargo se 
recomienda dar un vistazo al [esquema general del formato json](#esquema-tablas).

<h3>Estructura General</h3>

La información a imprimir y sus estilos se describen el objeto json que espera como parametro el constructor
de SweetTableDesign [a continuación](#esquema-tablas) se describe todas las propiedades configurables del json.
La mayoria de propiedades es opcional y tienen valores por defecto.

<h4 id="esquema-tablas">Esquema del objecto json para SweetTableDesign</h4>

El siguiente esquema se representó usando la sintaxis de TypeScrypt

```typescript

type Justification = "center" | "right" | "left"

// ##### ESQUEMA PRINCIPAL  #####
interface Report{
	printer: {
		properties: { // opcional
			width: int // Opcional, por defecto 42
			backgroundInverted: boolean;// Opcional, por defecto true, no se usa en el diseño de tablas de momento
			charCodeTable: string;// Opcional, por defecto WP1252
			textNormalize: boolean; // opcional por defecto false
		}
		title: string | string[]; // opcional, por defecto es un array vacio
		details: string | string[]; // opcional, por defecto es un array vacio
		tables: Table[]; // por defecto un array vacio
		finalDetails: (string | FinalDetail)[]; // opcional, por defecto un array vacio
	}
}

interface FinalDetail{
	text: string; // el texto a imprimir, por defecto -> ""
	align: Justification; //  por defecto -> center
	bold: boolean; // aplicar negrita, por defecto false
}

// #### ESQUEMA DE UN OBJECTO TABLE ####

interface Table{

	// representa el caracter que se imprimirá para separar las columnas
	// por defecto un espacio vacio -> ' '
	// si se envia un string, solo se tomara en cuenta el primer caracter del string
	separator: char; 

	// un array de enteros del 0 al 100, por defecto un array vacio
	// el indice '0' del array le corresponde a la columna 1, el indice '1' a la columna 2, sucesivamente...
	// si uno de los elementos tiene valor 0 entonces su ancho se calcula de forma automatica en funcion del porcentage restante.
	// algunos ejemplos a continuación suponiendo una tabla de 3 columnas:
	// si se envia [80] se convierte en -> [80, 0, 0] -> [80, 10, 10].
	// si se envia [] se convierte en -> [0, 0, 0] -> [33, 33, 33].
	// si se envia [40, 0, 20] se convierte en -> [40, 60, 20].
	widthPercents: int[]; 

	// cellBodyStyles, es aplicable solo al body, es un objeto que tiene como propiedades los indices de las columnas 
	// la propiedad '0' le corresponde a la columna 1 y asi sucevivamente.
	// por defecto todos los indices tienen el valor CellBodyStyle por defecto, 
	cellBodyStyles: {
		'0': Justification | CellBodyStyle,
		'1': Justification | CellBodyStyle,
		'4': Justification | CellBodyStyle
	};
	title: string | TitleTable;
	headers: (string | HeaderTable)[] // por defecto un array vacio

	// body representa las filas
	// body se comporta un matriz, cada elemento puede ser un array de strings (una fila)
	// algunos elementos pueden ser un Subtitle object (ver la interfaz declarada mas abajo)
	// algunos elementos pueden ser string, se imprimiran con los estilos de un Subtitle por defecto
	
	body: Array< (string | BodyCell)[] | Subtitle | string >; // por defecto es un array vacio y no se imprime
	footer: (string | FooterItem)[]; // por defecto un array vacio
}

interface FooterItem{
	text: string; // por defecto un string vacio

	// cuantas columnas debe ocupar, segun el maximo numero de columnas entre los headers y el body
	span: int;  // por defecto 1
	align: Justification; // por defecto left
	bold: boolean; // pintar en negrita el texto, por defecto false
}

interface BodyCell(){
	text: string; // por defecto un string vacio
	align: Justification; // por defecto left
}

interface Subtitle{
	text: string; // por defecto un string vacio
	align: Justification; // por defecto center
	bold: boolean; // pintar el texto en negrita, por defecto false
}

interface HeaderTable{
	text: string; // por defecto un string vacio
	align: Justification; // por defecto center
}

interface TitleTable{
	text: string; // por defecto false
	align: Justification; // por defecto center
	fontSize: int; // número entero del 1 al 7, por defecto 1

  // caracter decorador que envuelve al titulo
	// Si se envia un string solo se toma en cuenta el primer elemento
	// Si no se quiere un decorador se tiene que enviar un string vacio el valor ""
	decorator: char; // por defecto '-'
}

interface CellBodyStyle{
	align: Justification; // por defecto left
	bold: boolean; // pintar en negrita, por defecto false
}


```

<h2 id="referencia-puka">📦 Utilidad de impresión: Puka-http </h2>

La API de SweetTicketDesign y SweetTableDesign son muy simples y solo consiste en llamar a su constructor con el json y luego getBytes().
Toda la complejidad esta en armar el objeto json, y en enviar esto a una ticketera correspondiente. Conectarse con diferentes tipos de ticketeras es 
algo que **no** ofrece SweetTicketDesign, para ello existe otra herramienta 

<h3>Puka HTTP</h3>

Puka http es una herramienta multiplataforma que actua como servicio de impresión local e implementa una **API http** para trabajos de impresión de tickets y reportes.
PukaHTTP usa por debajo SweetTicketDesign para emitir los comandos EscPOS. Asi mismo soporta diferentes interfaces de conexión a ticketeras como 
ethernet, usb, samba , samba nativo, cups, linux-usb, etc. Para mas detalles en su [repositorio de github](https://github.com/puyu-pe/puka-http).

Se puede descargar el instalador multiplataforma desde [su pagina de descarga.](https://www.jdeploy.com/gh/puyu-pe/puka-http).
