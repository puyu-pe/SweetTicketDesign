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
SweetTicketDesign implementa interfaces e implementaciones para el diseño tickets responsivos que pueden ser emitidos a impresoras termicas.
Trae incluido 
- EscPosPrinter: Una implementación de SweetPrinter para renderizar los diseños en ticketeras termicas
  usando comandos escpos usando la libreria [escpos coffee](https://github.com/anastaciocintra/escpos-coffee)
- GsonPrinterObjectBuilder: Una implementación de SweetPrinterObjectBuilder para construción de componentes de impresión 
  a travéz archivos json.
- DefaultComponentsProvider: Un proveedor de componentes de impresión con valores aceptables por defecto.

1. [Empezando](#empezando)
2. [Uso basico](#uso-basico)
3. [Extra: Utilidad de impresión](#pukahttp)

✨
## Empezando

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

📚 
## Uso Basico

Toda la logica de diseño esta implementada en la clase SweetDesigner, esta clase depende de 3 interfaces para lo cual se tiene que crear 3 implentaciones de esas 3 interfaces.
Sin embargo SweetTicketDesign ya crea implementaciones basicas comunes para esas 3 interfaces. A contuniación un ejemplo de como ser veria:


```java
import com.github.anastaciocintra.output.PrinterOutputStream;
import com.github.anastaciocintra.output.TcpIpOutputStream;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pe.puyu.SweetTicketDesign.application.components.DefaultComponentsProvider;
import pe.puyu.SweetTicketDesign.application.builder.gson.GsonPrinterObjectBuilder;
import pe.puyu.SweetTicketDesign.application.printer.escpos.EscPosPrinter;
import pe.puyu.SweetTicketDesign.domain.designer.SweetDesigner;
import pe.puyu.SweetTicketDesign.domain.printer.SweetPrinter;

import javax.print.PrintService;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        try (OutputStream outputStream = ip("192.168.1.38")) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            testSweetDesigner(byteArrayOutputStream);
            outputStream.write(byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testSweetDesigner(OutputStream outputStream) throws FileNotFoundException {
        String pathToFile = "/home/your_user/samples/designer.json";
        FileReader reader = new FileReader(pathToFile);
        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        GsonPrinterObjectBuilder builder = new GsonPrinterObjectBuilder(jsonObject);
        SweetPrinter printer = new EscPosPrinter(outputStream);
        SweetDesigner designer = new SweetDesigner(builder, printer, new DefaultComponentsProvider());
        designer.paintDesign();
    }

    private static TcpIpOutputStream ip(String ip) throws IOException {
        return new TcpIpOutputStream(ip);
    }

}
```

En este caso el diseño será impreso en una ticketera termica en Red asociado al ip 192.168.1.38 y construye componentes de impresión teniendo como entrada un archivo json.

Basicamente SweetTicketDesign espera como parametros:

- Un constructor de componentes de impresión, como **GsonPrinterObjectBuilder** que se encarga de construir los componentes de impresión mediante la lectura de un archivo en formato json.
- Un objeto Printer para renderizar los diseños, como **EscPosPrinter** que se encarga de renderizar los diseños en ticketeras termicas usando comandos escpos.
- Un Proveedor de componentes por defecto, como **DefaultComponentsProvider** que define valores por defecto minimos para los diseños de impresión.

📦
## PukaHttp

Puka Http es una herramienta multiplataforma que actua como servicio de impresión. Implementa una Api http para la impresión de ticketes en impresoras termicas y 
usa SweetTicketDesign para el diseño de los tickets. Puka Http maneja la comunicación a diferentes tipos de impresoras termicas (red, usb, samba, etc.)

Se puede descargar el instalador multiplataforma desde [su pagina de descarga.](https://www.jdeploy.com/gh/puyu-pe/puka-http).
