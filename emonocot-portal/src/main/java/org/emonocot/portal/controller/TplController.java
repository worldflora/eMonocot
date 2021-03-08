package org.emonocot.portal.controller;

import org.emonocot.api.TaxonService;
import org.emonocot.model.Taxon;
import org.emonocot.portal.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/tpl")
public class TplController extends GenericController<Taxon, TaxonService> {

    private static Logger queryLog = LoggerFactory.getLogger("query");

    public TplController() {
        super("taxon", Taxon.class);
    }

    @Autowired
    public void setTaxonService(TaxonService taxonService) {
        super.setService(taxonService);
    }

    @RequestMapping(value = "/{majorGroup}/{tplFamily}", method = RequestMethod.GET, produces = {"text/html", "*/*"})
    public String tplShowFamily(@PathVariable String majorGroup, @PathVariable String tplFamily, Model model) throws Exception {
        String identifier = null;
        Taxon persistedTaxon = getService().findByTplID(majorGroup, tplFamily);

        if (persistedTaxon != null) {
            identifier = persistedTaxon.getIdentifier();

            model.addAttribute(getService().load(identifier, "object-page"));
            queryLog.info("Taxon: \'{}\'", new Object[]{identifier});
        } else {
            //queryLog.error("Cannot find tplID " + tplID);
            throw new Exception("exception in TPL redirect");
        }
        return "redirect:/taxon/" + identifier;
    }

    @RequestMapping(value = "/{majorGroup}/{tplFamily}/{tplGenus}", method = RequestMethod.GET, produces = {"text/html", "*/*"})
    public String tplShowGenus(@PathVariable String majorGroup, @PathVariable String tplFamily, @PathVariable String tplGenus, Model model) throws Exception {
        String identifier = null;
        try {
            Taxon persistedTaxon = getService().findByTplID(majorGroup, tplFamily, tplGenus);

            if (persistedTaxon != null) {
                identifier = persistedTaxon.getIdentifier();

                model.addAttribute(getService().load(identifier, "object-page"));
                queryLog.info("Taxon: \'{}\'", new Object[]{identifier});
            }
            return "redirect:/taxon/" + identifier;
        } catch (ResourceNotFoundException ex) {
            throw new Exception("Exception in TPL redirect. Exception message" + ex.getMessage());
        }
    }

    @RequestMapping(value = "/{majorGroup}", method = RequestMethod.GET, produces = {"text/html", "*/*"})
    public String tplShowSpeciesOrMajorGroup(@PathVariable String majorGroup, Model model) throws Exception {
        String identifier = null;
        Taxon persistedTaxon = getService().findByTplID(majorGroup);
        if (persistedTaxon != null) {
            identifier = persistedTaxon.getIdentifier();
            model.addAttribute(getService().load(identifier,  "object-page"));
            queryLog.info("Taxon: \'{}\'", new Object[]{identifier});
        } else {
            throw new Exception("exception in TPL redirect");
        }
        return "redirect:/taxon/" + identifier;
    }
}
