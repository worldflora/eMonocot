package org.emonocot.job.dwc.reference;

import java.util.HashMap;

import org.easymock.EasyMock;
import org.emonocot.job.dwc.description.DescriptionFieldSetMapper;
import org.emonocot.model.description.TextContent;
import org.emonocot.model.reference.Reference;
import org.emonocot.model.taxon.Taxon;
import org.emonocot.api.ReferenceService;
import org.emonocot.api.TaxonService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author ben
 *
 */
public class ReferenceParsingTest {

   /**
    *
    */
   private Resource content = new ClassPathResource(
           "/org/emonocot/job/dwc/reference.txt");

   /**
    *
    */
   private TaxonService taxonService = null;

   /**
    *
    */
    private FlatFileItemReader<Reference> flatFileItemReader = new FlatFileItemReader<Reference>();

   /**
    * @throws Exception if there is a problem
    */
   @Before
   public final void setUp() throws Exception {

       String[] names = new String[] {
               "http://rs.tdwg.org/dwc/terms/taxonID",
               "http://purl.org/dc/terms/modified",
               "http://purl.org/dc/terms/created",
               "http://purl.org/dc/terms/identifier",
               "http://purl.org/dc/terms/bibliographicCitation",
               "http://purl.org/dc/terms/type",
               "http://purl.org/dc/terms/title",
               "http://purl.org/ontology/bibo/volume",
               "http://purl.org/ontology/bibo/number",
               "http://purl.org/ontology/bibo/pages",
               "http://purl.org/dc/terms/description",
               "http://purl.org/dc/terms/date",
               "http://purl.org/dc/terms/source",
               "http://purl.org/dc/terms/creator"
       };
       DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
       tokenizer.setDelimiter('\t');
       tokenizer.setNames(names);

       taxonService = EasyMock.createMock(TaxonService.class);

        ReferenceFieldSetMapper fieldSetMapper = new ReferenceFieldSetMapper();
        fieldSetMapper.setFieldNames(names);
        fieldSetMapper.setDefaultValues(new HashMap<String, String>());
        fieldSetMapper.setTaxonService(taxonService);
        DefaultLineMapper<Reference> lineMapper
            = new DefaultLineMapper<Reference>();
        lineMapper.setFieldSetMapper(fieldSetMapper);
        lineMapper.setLineTokenizer(tokenizer);

        flatFileItemReader.setEncoding("UTF-8");
        flatFileItemReader.setLinesToSkip(0);
        flatFileItemReader.setResource(content);
        flatFileItemReader.setLineMapper(lineMapper);
        flatFileItemReader.afterPropertiesSet();
   }

    /**
     * @throws Exception if there is a problem accessing the file
     */
    @Test
    public final void testRead() throws Exception {
        EasyMock.expect(taxonService.find(EasyMock.isA(String.class))).andReturn(new Taxon()).anyTimes();
        EasyMock.replay(taxonService);
        flatFileItemReader.open(new ExecutionContext());
        flatFileItemReader.read();

    }

}