package org.emonocot.portal.controller;

import org.emonocot.api.TaxonService;
import org.emonocot.model.Taxon;
import org.emonocot.pager.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class IpniController extends GenericController<Taxon, TaxonService> {

    private static Logger queryLog = LoggerFactory.getLogger("query");

    public IpniController() {
        super("taxon", Taxon.class);
    }

    @Autowired
    public void setTaxonService(TaxonService taxonService) {
        super.setService(taxonService);
    }

    @RequestMapping(value = "/ipni/{scientificNameID}", method = RequestMethod.GET, produces = {"text/html", "*/*"})
    public String ipniShow(@PathVariable String scientificNameID, Model model) throws Exception {
        String identifier = null;
        Page<Taxon> persistedTaxon = getService().findByScientificNameID(scientificNameID);

        if (persistedTaxon != null) {
            if (persistedTaxon.getSize() > 1) {
                model.addAttribute("result", persistedTaxon);
                return "redirect:/search?facet=base.class_s%3aorg.emonocot.model.Taxon";
            } else {
                identifier = persistedTaxon.getRecords().iterator().next().getIdentifier();
                model.addAttribute(getService().load(identifier, "object-page"));
                queryLog.info("Taxon: \'{}\'", new Object[]{identifier});
                return "redirect:/taxon/" + identifier;
            }
        } else {
            throw new Exception("exception in IPNI redirect");
        }
    }

//    @RequestMapping(value = "/ipni/{scientificNameID}", method = RequestMethod.GET, produces = {"text/html", "*/*"})
//    public String ipniShow(@PathVariable String scientificNameID, Model model) throws Exception {
//        String identifier = null;
//        Taxon persistedTaxon = getService().findByScientificNameID(scientificNameID);
//
//        if (persistedTaxon != null) {
//            identifier = persistedTaxon.getIdentifier();
//
//            model.addAttribute(getService().load(identifier, "object-page"));
//            queryLog.info("Taxon: \'{}\'", new Object[]{identifier});
//        } else {
//            throw new Exception("exception in IPNI redirect");
//        }
//        return "redirect:/taxon/" + identifier;
//    }
//

    @RequestMapping(value = "/tpl/{tplID}", method = RequestMethod.GET, produces = {"text/html", "*/*"})
    public String tplShowSpecies(@PathVariable String tplID, Model model) throws Exception {
        String identifier = null;
        try {
            Taxon persistedTaxon = getService().findByTplID(tplID);

            if (persistedTaxon != null) {
                identifier = persistedTaxon.getIdentifier();

                model.addAttribute(getService().load(identifier, "object-page"));
                queryLog.info("Taxon: \'{}\'", new Object[]{identifier});
            }
            //return "redirect:/taxon/" + identifier;
        } catch (Exception ex) {
            // if(StringUtils.containsIgnoreCase(ex.getCause().getCause().getMessage(), "identifier" )){
            String[] codes = new String[]{"No row with the given identifier exists {0} "};
            Object[] args = new Object[]{tplID};
            DefaultMessageSourceResolvable message = new DefaultMessageSourceResolvable(codes, args);
            model.addAttribute("error", message);
            //}
            //return "redirect:/taxon/" + identifier;

            //throw new Exception("No row with the given identifier exists. Exception message"+ ex.getMessage());
        }
        return "redirect:/taxon/" + identifier;
    }

//    @RequestMapping(value = "/tpl/{tplID}", method = RequestMethod.GET, produces = {"text/html", "*/*"})
//    public String tplShowSpecies(@PathVariable String tplID, Model model) throws Exception {
//        String identifier = null;
//
//        Taxon persistedTaxon = getService().findByTplID(tplID);
//
//        if (persistedTaxon != null) {
//            identifier = persistedTaxon.getIdentifier();
//
//            model.addAttribute(getService().load(identifier, "object-page"));
//            queryLog.info("Taxon: \'{}\'", new Object[]{identifier});
//        } else {
//            queryLog.error("Cannot find tplID " + tplID);
//            throw new Exception("exception in TPL redirect");
//        }
//        return "redirect:/taxon/" + identifier;
//    }
}
