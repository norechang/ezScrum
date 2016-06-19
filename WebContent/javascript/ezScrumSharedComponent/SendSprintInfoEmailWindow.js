Ext.ns('ezScrum');
Ext.ns('ezScrum.window');
Ext.ns('ezScrum.layout');

var PartnerStore_ForSprintInfo = new Ext.data.Store({
    id: 0,
    fields: [
   		{name: 'Name'}
    ],
    reader : PartnersReader
});

var PartnerTriggerField_SprintInfo = new Ext.form.TriggerField({
    fieldLabel : 'Receivers (ezScrum members)',
    name : 'Receivers (ezScrum members)',
    editable   : false
});
PartnerStore_ForSprintInfo.on('load', function(store, records, options) {
	PartnerMenuForSprintInfo.removeAll();
	
	for(var i=0; i<this.getCount(); i++) {
		var record = this.getAt(i);
		var info = record.get('Name');

		PartnerMenuForSprintInfo.add({
			id		: info,
			tagId 	: info,
			text	: info,
			xtype	: 'menucheckitem',
			hideOnClick	: false,
			checkHandler : PartnerMenuForSprintInfo.onCheckItemClick
		});
	}
});
PartnerTriggerField_SprintInfo.onTriggerClick = function() {
	
	// A array of items of the menu
	var checkedItem = Ext.getCmp('PartnerMenu').findByType('menucheckitem');

	// the name list of the project team
	var partnerMenuList = PartnerTriggerField_SprintInfo.getValue().split(';');
	
	// initial the checked items
	for(var i=0; i<checkedItem.length; i++) {
		Ext.getCmp('PartnerMenu').get(checkedItem[i].text).setChecked(false);
	}
	
	// 將 field 欄位中的有的 partner, 在其對應的 menu item 打勾
	for(var i=0; i<checkedItem.length; i++) {
		for(var j=0; j<checkedItem.length; j++) {
			if(partnerMenuList[i] == checkedItem[j].text) {
				Ext.getCmp('PartnerMenu').get(checkedItem[j].text).setChecked(true);
			}
		}
	}
	PartnerMenuForSprintInfo.showAt(PartnerTriggerField_SprintInfo.getPosition());
};


var PartnerMenuForSprintInfo = new Ext.menu.Menu({
	/*
     * When CheckItem was checked，update TagTriggerField's text
     */
	id: 'PartnerMenu',
	onCheckItemClick : function(item, checked){
		var tagRaw = PartnerTriggerField_SprintInfo.getValue();
		var tags;
		if (tagRaw.length != 0) {
            tags = tagRaw.split(";");
        } else {
            tags = [];
        }
		if (checked) {
        	if(tagRaw.search(item.text)<0) {
        		// if field has text, should not check item
            	tags.push(item.text);
        	}
        } else {
            var index = tags.indexOf(item.text);
            tags.splice(index, 1);
        }
        PartnerTriggerField_SprintInfo.setValue(tags.join(";"));
	},
	loadPartnerList : function() {
		// to request partner list
		Ext.Ajax.request({
			url: 'AjaxGetPartnerList.do',
			success: function(response) {
				PartnerStore_ForSprintInfo.loadData(response.responseXML);
			}
		}); 
	}
});

ezScrum.SprintInfoForm = Ext.extend(Ext.form.FormPanel, {
	bodyStyle: 'padding:15px',
	labelWidth : 100,
	notifyPanel: undefined, // notify panel
	defaultType: 'textfield',
	labelAlign : 'right',
	monitorValid: true,
	defaults: {
		width: 500,
		msgTarget: 'side'
	},
	initComponent: function() {
		var config = {
				items: [{
					fieldLabel: 'Sender Email',
		            name: 'Sender Email',
		            allowBlank: false,
		            maxLength: 128
		        },{
		        	fieldLabel: 'Sender Password',
		            name: 'Sender Password',
		            allowBlank: false,
		            maxLength: 128
		        }, 
		        PartnerTriggerField_SprintInfo,
		        {
		            name: 'sprintId',
		            hidden: true
		        }],
		        buttons:
		        [{
		        	formBind:true,
		    		text: 'Send',
		    		scope:this,
		    		disabled: true,
		    		handler: this.send
		        },{
		    		text: 'Cancel',
		    		scope:this,
		    		handler: function(){this.ownerCt.hide();}
		        }]
		}
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		ezScrum.SprintInfoForm.superclass.initComponent.apply(this, arguments);
	},
	send : function()
	{
		var obj = this;
		var form = this.getForm();
		Ext.Ajax.request({
			//url:this.url,
			params:form.getValues(),
			success:function(response){},
			failure:function(response){}
		});
	},
	reset: function(){
		this.getForm().reset();
		PartnerMenuForSprintInfo.loadPartnerList();
	}
});
Ext.reg('sprintInfoForm', ezScrum.SprintInfoForm);

ezScrum.window.SendSprintInfoEmailWindow = Ext.extend(ezScrum.layout.Window, {
	title: 'Send Sprint Info',
	initComponent: function(){
		var config = {
			layout:'form',
			items : [{xtype:'sprintInfoForm'}]
		}
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		ezScrum.window.SendSprintInfoEmailWindow.superclass.initComponent.apply(this, arguments);
		
		this.SprintInfoForm = this.items.get(0);
	},
	showTheWindow: function(panel, sprintID){
		this.SprintInfoForm.notifyPanel = panel;
		this.SprintInfoForm.reset();
		this.SprintInfoForm.getForm().setValues({SprintId: sprintID});
		
		this.show();
	}
});
var SendSprintInfoEmailWindow = new ezScrum.window.SendSprintInfoEmailWindow();