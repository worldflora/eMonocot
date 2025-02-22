Key.SimpleView = "Simple";
Key.ListView = "List";

Key.prototype.view = Key.SimpleView;

Key.prototype.getView = function() {
	return this.view;
};

Key.prototype.setView = function(view) {
	this.view = view;
};

function writeNode(key, node) {
   var html = "";

   if(!Key.isUndefined(node.isExcluded) && node.isExcluded) {
	     return html;
   }
   if(!Key.isUndefined(node.concept)) {
	  var children = "";
	  if(!Key.isUndefined(node.children)) {
         for(var i = 0; i < node.children.length; i++) {
	        var child = node.children[i];
	        children += writeNode(key, child); 
	     }
	  }
	  if(children.length == 0) {
		 return "";
      }
      html += "<li class='descriptiveConcept media'><div class='collapsed' data-toggle='collapse' data-target='#node" + node.id +"'>";
      if(!Key.isUndefined(node.images) && node.images.length > 0) {
         var image = node.images[0];
         var imageId = image.href.substring(0,image.href.indexOf('.'));
         html += "<img id='descriptiveConcept" + node.id + "' data-link='" + key.getImagePath() + imageId + "' class='thumbnail pull-right' src='" + key.getThumbnailImagePath() +  image.href + "' title='" + node.concept + "'/>";     
         html += "<div class='media-body pull-left'><a><i class='halflings-icon plus'></i>" + node.concept + "</a></div>";   
     } else {
         html += "<div class='media-body pull-left'><a><i class='halflings-icon plus'></i>" + node.concept + "</a></div>";
     }
     html += "</div>";
     html += "<div id='node" + node.id + "' class='collapse'><ul class='unstyled'>";
     
     html += children;
     html += "</ul></div></li>";
   } else {
     var character = key.getCharacter(node.character);
     if((!character.selectedValues || character.selectedValues.length == 0) && !character.isRedundant) {
       html += "<li class='character media'>";
       if(!Key.isUndefined(character.images) && character.images.length > 0) {
         var image = character.images[0];
         var imageId = image.href.substring(0,image.href.indexOf('.'));
         html  += "<div class='media-body pull-left'><a id='" + character.id + "'><i class='halflings-icon unchecked'></i>" + character.name + "</a></div>";
         html  += "<img id='character" + character.id + "' data-link='" + key.getImagePath() + imageId + "' class='thumbnail pull-right' src='" + key.getThumbnailImagePath() +  image.href + "' title='" + character.name + "'/>";
         
       } else {
         html  += "<div class='media-body'><a id='" + character.id + "'><i class='halflings-icon unchecked'></i>" + character.name + "</a></div>";
       }
       html += "</li>";
     }
   }
   return html;
}

function characterModal(characterId, key) {
    var character = key.getCharacter(characterId);
    $('#characterModal .modal-header h3').html(character.name);
    var body = "";
    switch(character.type) {
      case Key.Categorical:      
      var imageIndex = 0;
      for(var i = 0; i < character.states.length; i++) {
        var state = character.states[i];
        body += "<div class='media'>";
        if(!Key.isUndefined(state.images) && state.images.length > 0) {
            var image = state.images[0];
            var imageId = image.href.substring(0,image.href.indexOf('.'));
            var description = !Key.isUndefined(image.description) ? image.description : '';
            body += "<a class='pull-right' href='#'><img id='character" + character.id + "-" + i + "-" + imageIndex + "' class='thumbnail' data-icon='icon-white icon-picture' src='" + key.getThumbnailImagePath() +  image.href + "'title='" + image.caption + "' data-description='" + description + "'/></a>";
            for(var j =0; j < state.images.length; j++) {
          	   imageIndex++;
            }
        }
        body += "<div class='media-body'><div class='control-group'><div class='controls'><label class='checkbox'><input type='checkbox'>" + state.name + "</label>";
        if(!Key.isUndefined(state.description)) {
           	   body += "<span class='help-block'>" + state.description + "</span>";
        }
        body += "</div></div></div></div>";	
        
      }
      $('#characterModal .modal-body').html(body);
      $('#save').unbind("click");
      $('#save').click(function() {
        var s = 1;
        var selectedValues = [];
        $('#characterModal .modal-body input').each(function() {
          if($(this).is(':checked')) {
            selectedValues.push(s);
          }
          s++;
        });
        key.selectCharacter(character.id,selectedValues);
        key.calculate();             
        $('#characterModal').modal('hide');
        return false;
      });      
      var galleryBody = "";
      for (var i=0; i< character.states.length; i++){
        var state = character.states[i];
        if(!Key.isUndefined(state.images) && state.images.length > 0) {
           for(var j = 0; j < state.images.length; j++) {
               var imageId = state.images[j].href.substring(0,state.images[j].href.indexOf('.'));
               var description = !Key.isUndefined(state.images[j].description) ? state.images[j].description : '';
               galleryBody += "<a href='" + key.getFullsizeImagePath() +  state.images[j].href + "' data-link='" + key.getImagePath() +  imageId + "' data-icon='icon-white icon-picture' rel='gallery' title='" + state.images[j].caption + "' data-description='" + description + "'>" + state.name + "</a>";
           }
        }
      }
      
      $('#gallery').html(galleryBody);

      $("#characterModal .thumbnail").click(function(event) {
         var id = event.target.id;
         var temp = new Array();
         temp = id.split('-');
         var characterId = temp[0].substring(9);
         
         var stateIndex = temp[1];
         var imageIndex = temp[2];
         var character = key.getCharacter(characterId);
         
         $('#characterModal').modal('hide');
         
         var options = {target:"#modal-gallery", slideshow:"5000", selector:"#gallery a[rel=gallery]", index: imageIndex};
         var modal = $('#modal-gallery');
         
         options = jQuery.extend(modal.data(), options);
         
         modal.on('hidden', function() {
       	  $('#characterModal').modal('show');
       	  $('#modal-gallery').data('modal', null);
         });
         modal.find('.modal-slideshow').find('i').removeClass('icon-play').addClass('icon-pause');
         modal.modal(options);
         return false;
       });
      
       $('body').keydown(function(e) {    	   
    	   if(e.which == 27) {
    		   e.preventDefault();
    		   var func = arguments.callee;
               $('body').unbind("keydown",func);
    	       $('#characterModal').modal('hide');
    	       return false;
    	   }
       });
      
       $('#characterModal').modal({});
       break;
       default:
       // Continuous
       body += "<div class='row'><label class='span2' for='quantitative'>Enter a value between " + character.min + " and " + character.max + "</label>"   
       body += "<input name='quantitative' type='text' class='span3' placeholder='Type something'/><span class='help-inline'>" + character.unit + "</span></div>";
       $('#characterModal .modal-body').html(body);
       $('#save').unbind("click");
       $('#save').click(function() {
          var value = $('#characterModal .modal-body input').val();
          key.selectCharacter(character.id,value);
          key.calculate();
          $('#characterModal').modal('hide');
          return false;
       });
       
       $('body').keydown(function(e) {
    	   if(e.which == 27) {
    		   e.preventDefault();
    		   var func = arguments.callee;
               $('body').unbind("keydown",func);
    	       $('#characterModal').modal('hide');
    	       return false;
    	   }
       });
       $('#characterModal .modal-body input').unbind('keydown');
       $('#characterModal .modal-body input').keydown(function (e) {
    	   if (e.which == 13) {
    		   e.preventDefault();
    		   var value = $('#characterModal .modal-body input').val();
               key.selectCharacter(character.id,value);
               key.calculate();     
               var func = arguments.callee;
               $('#characterModal .modal-body input').unbind("keydown",func);
               $('#characterModal').modal('hide');
               return false;
    	   }
    	 });
       $('#characterModal').modal({});
       break;
     }
}

function updateUI(key) {
      var selectedCharacters = key.getSelectedCharacters();
      var unselectedCharacters = key.getUnselectedCharacters();
      var matchedTaxa = key.getMatchedTaxa();
      var unmatchedTaxa = key.getUnmatchedTaxa();
      var characterTree = key.getCharacterTree();

      var matched = "";
      
      for(var i = 0; i < matchedTaxa.length; i++) {
        var taxon = matchedTaxa[i];
        matched += "<tr>";
        matched +="<td><img src=\"../css/images/glyphicons/halfsize/glyphicons_001_leaf.png\" alt=\"Taxon\" style=\"width:20px ; height:20px\"/></td>";
        if(!Key.isUndefined(taxon.links) && taxon.links.length > 0 && taxon.links[0].href != "") {
            var link = taxon.links[0];
            matched += "<td><a href='" + key.getTaxonPath() + link.href + "' title='" + link.title + "'><h4>" + taxon.name + "</h4></a></td>";
        } else {
            matched += "<td><h4>" + taxon.name + "</h4></td>";
        }
        if(key.getView() == Key.ListView ) {
            if (!Key.isUndefined(taxon.images) && taxon.images.length > 0){
               var image = taxon.images[0];
               matched += "<td><a class='pull-right' href='#'><img class='thumbnail' src='" + key.getThumbnailImagePath() +  image.href + "'/></a></td>";
        	} else{
        		matched += "<td><img class='thumbnail pull-right' src=\"../css/images/no_image.jpg\"/></td>";
        	}
        } else {
        	matched += "<td></td>";
        }        
        matched += "</tr>";
      }
      $("#matchedTaxa table tbody").html(matched);
      $("#pages").html(matchedTaxa.length + " taxa remaining");
      
      var unSelected = "";
      var available = 0;
      for(var i = 0; i < unselectedCharacters.length; i++) {
        var character = unselectedCharacters[i];
        if(!character.isRedundant && !key.isExcluded(character)) {
        	available++;
        }
      }
      for(var i = 0; i < characterTree.length; i++) {
         unSelected += writeNode(key, characterTree[i]);
      }
      
	  $("#unselectedCharacters").html("<li class='nav-header'>Features Available: " + available + "</li>" + unSelected);
      
      $("#unselectedCharacters li.character a").click(function(event) {
          characterModal(event.target.id, key); 
      });
      /**
       * BUG #569 UI issue with selecting characters
       */
      $("#unselectedCharacters li.character a i").click(function(event) {
          characterModal(event.target.parentNode.id, key);
      });

      
      $(".thumbnail").click(function(event) {

          if(event.target.id.indexOf("character") == 0){
  
            var character = key.getCharacter(event.target.id.substring(9));

            var body = "";
            for(var i=0; i< character.images.length; i++){
                var image = character.images[i];
                var imageId = image.href.substring(0,image.href.indexOf('.'));
                var description = !Key.isUndefined(image.description) ? image.description : '';
                body += "<a data-icon='icon-white icon-picture' data-link='" + key.getImagePath() +  imageId + "' href='" + key.getFullsizeImagePath() +  image.href + "' rel='gallery' title='" + image.caption +"' data-description='" + description + "'>" + image.caption +"</a>";
            }
            
            $('#gallery').html(body);
          
          } else {
            var title = event.target.title;
           
            var descriptiveConcept = key.getDescriptiveConcept(event.target.id.substring(18));

            var body = "";
            for(var i=0; i< descriptiveConcept.images.length; i++){
                var image = descriptiveConcept.images[i];
                var imageId = image.href.substring(0,image.href.indexOf('.'));
                var description = !Key.isUndefined(image.description) ? image.description : '';
                body += "<a data-icon='icon-white icon-picture' data-link='" + key.getImagePath() +  imageId + "' href='" + key.getFullsizeImagePath() +  descriptiveConcept.images[i].href + "' rel='gallery' title='" + image.caption + "' data-description='" + description + "'>" + image.caption +"</a>";
            }
            $('#gallery').html(body);
            
          }
          $('#modal-gallery').unbind('hidden');
         
          var options = {target:"#modal-gallery", slideshow:"5000", selector:"#gallery a[rel=gallery]", index: 0};
          var modal = $('#modal-gallery');
          options = jQuery.extend(modal.data(), options);
          modal.find('.modal-slideshow').find('i').removeClass('icon-play').addClass('icon-pause');
          modal.modal(options);
          return false;
        });

        var selected = "";
        for(var i = 0; i < selectedCharacters.length; i++) {
          var character = selectedCharacters[i];
          if(!Key.isUndefined(character)) {
             selected += "<li class='selectedCharacter'><a id='" + character.id + "'><i class='halflings-icon check'></i>"  + character.name + ": ";
             switch(character.type) {
                case Key.Categorical:
                   var values;
                   if($.isArray(character.selectedValues)) {
                      values = character.selectedValues;
                   } else {
                      values = [];
                      values.push(character.selectedValues);
                   }
                   for(var j = 0; j < values.length; j++) {
                      if(j > 0) {
                          selected += ", ";
                      }
                      var state = character.states[values[j] - 1];
                      selected += state.name;
                      
                   }
                    break;
                    default:
                    // categorical
                    selected += character.selectedValues + " " + character.unit;
                    break;
                  }
                  selected += "</a></li>";
               }
           }      
          $("#selectedCharacters").html("<li class='nav-header'>Features Chosen: " + selectedCharacters.length + "</li>" + selected);
          $("#selectedCharacters li.selectedCharacter a").click(function(event) {
          key.unselectCharacter(event.target.id);
          key.calculate();
      });
}