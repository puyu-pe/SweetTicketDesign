module pe.puyu.SweetTicketDesign {
    requires com.google.gson;
    requires escpos.coffee;
    requires java.desktop;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires org.jetbrains.annotations;
    exports pe.puyu.SweetTicketDesign.domain.designer;
    exports pe.puyu.SweetTicketDesign.application.builder.gson;
    exports pe.puyu.SweetTicketDesign.application.components;
    exports pe.puyu.SweetTicketDesign.application.printer.escpos;
}
