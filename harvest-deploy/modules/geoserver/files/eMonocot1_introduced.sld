<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0"
 xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd"
 xmlns="http://www.opengis.net/sld"
 xmlns:ogc="http://www.opengis.net/ogc"
 xmlns:xlink="http://www.w3.org/1999/xlink"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <!-- a Named Layer is the basic building block of an SLD document -->
  <NamedLayer>
    <Name>default_polygon</Name>
    <UserStyle>
    <!-- Styles can have names, titles and abstracts -->
      <Title>Default Polygon</Title>
      <Abstract>A sample style that draws a polygon</Abstract>
      <!-- FeatureTypeStyles describe how to render different features -->
      <!-- A FeatureTypeStyle for rendering polygons -->
      <FeatureTypeStyle>
        <Rule>
          <Name>Fill</Name>
          <Title>Brown Fill</Title>
          <Abstract>A polygon with green fill</Abstract>
          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill">#9A9261</CssParameter>
            </Fill>           
          </PolygonSymbolizer>
        </Rule>
        <Rule>
          <Name>Stroke</Name>
          <Title>Green Outline</Title>
          <Abstract>A polygon 1 pixel white outline (scale dependant)</Abstract>
          <PolygonSymbolizer>            
            <Stroke>
              <CssParameter name="stroke">#FFFFFF</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
              <CssParameter name="stroke-opacity">0.5</CssParameter>
            </Stroke>
          </PolygonSymbolizer>
        </Rule>
        <!--<Rule>
          <Name>Text</Name>
          <Title>The Region Name in White</Title>
          <Abstract>The feature name in white (scale dependant)</Abstract>       
          <TextSymbolizer>
            <Label>
             <ogc:PropertyName>level1_nam</ogc:PropertyName>
            </Label>
            <Font>
             <CssParameter name="font-family">Lucida Sans</CssParameter>
             <CssParameter name="font-size">14</CssParameter>
             <CssParameter name="font-style">normal</CssParameter>
             <CssParameter name="font-weight">bold</CssParameter>
           </Font>
            <LabelPlacement>
             <PointPlacement>
               <AnchorPoint>
                 <AnchorPointX>0.5</AnchorPointX>
                 <AnchorPointY>0.5</AnchorPointY>
               </AnchorPoint>
             </PointPlacement>
           </LabelPlacement>
            <Halo>
           <Radius>2</Radius>
           <Fill>
             <CssParameter name="fill">#000000</CssParameter>
           </Fill>
         </Halo>
           <Fill>
             <CssParameter name="fill">#FFFFFF</CssParameter>
           </Fill>
            <VendorOption name="autoWrap">60</VendorOption>
           <VendorOption name="maxDisplacement">150</VendorOption>
          </TextSymbolizer>          
        </Rule>-->
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>