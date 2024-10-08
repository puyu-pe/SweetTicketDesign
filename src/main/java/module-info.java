module pe.puyu.SweetTicketDesign {
    requires com.google.gson;
    requires escpos.coffee;
    requires java.desktop;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires org.jetbrains.annotations;
    exports pe.puyu.SweetTicketDesign.domain.designer;
    exports pe.puyu.SweetTicketDesign.domain.components;
    exports pe.puyu.SweetTicketDesign.domain.components.drawer;
    exports pe.puyu.SweetTicketDesign.domain.components.properties;
    exports pe.puyu.SweetTicketDesign.domain.printer;
    exports pe.puyu.SweetTicketDesign.domain.builder;
    exports pe.puyu.SweetTicketDesign.application.builder.gson;
    exports pe.puyu.SweetTicketDesign.application.components;
    exports pe.puyu.SweetTicketDesign.application.printer.escpos;
    exports pe.puyu.SweetTicketDesign.domain.designer.qr;
    exports pe.puyu.SweetTicketDesign.domain.components.block;
    exports pe.puyu.SweetTicketDesign.domain.designer.img;
    exports pe.puyu.SweetTicketDesign.domain.designer.text;
}
