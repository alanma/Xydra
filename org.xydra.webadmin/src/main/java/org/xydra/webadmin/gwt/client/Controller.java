package org.xydra.webadmin.gwt.client;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.util.TableController;
import org.xydra.webadmin.gwt.client.util.TableController.Status;
import org.xydra.webadmin.gwt.client.util.TempStorage;
import org.xydra.webadmin.gwt.client.widgets.AddressWidget.CompoundActionCallback;
import org.xydra.webadmin.gwt.client.widgets.WarningWidget;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanel;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.SelectionTree;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;


public class Controller {
    
    private static Controller instance;
    private ServiceConnection service;
    private SelectionTree selectionTree;
    private EditorPanel editorPanel;
    private TempStorage tempStorage;
    private XAddress lastClickedElement;
    private WarningWidget warningWidget;
    private TableController tableController;
    
    private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
    
    public Controller() {
        this.tempStorage = new TempStorage();
    }
    
    /**
     * Use {@link XyAdmin#getController()}
     * 
     * @return ...
     */
    @Deprecated
    public static Controller getInstance() {
        if(instance == null)
            instance = new Controller();
        return instance;
    }
    
    public void addService(XyAdminServiceAsync service) {
        this.service = new ServiceConnection(service, this.tempStorage);
    }
    
    public void registerSelectionTree(SelectionTree selectionTree) {
        
        this.selectionTree = selectionTree;
    }
    
    public void registerEditorPanel(EditorPanel widget) {
        
        this.editorPanel = widget;
    }
    
    private void notifyEditorPanel(SessionCachedModel result) {
        this.editorPanel.notifyMe(result);
        
    }
    
    public TempStorage getTempStorage() {
        return this.tempStorage;
    }
    
    public XAddress getSelectedModelAddress() {
        return this.lastClickedElement;
    }
    
    public void loadCurrentModelsObjects(CompoundActionCallback compoundActionCallback) {
        loadModelsObjects(this.lastClickedElement, compoundActionCallback);
    }
    
    public void loadModelsObjects(XAddress address, CompoundActionCallback compoundActionCallback) {
        this.service.loadModelsObjects(address, compoundActionCallback);
    }
    
    public void updateEditorPanel() {
        SessionCachedModel model = DataModel.getInstance()
                .getRepo(Controller.this.lastClickedElement.getRepository())
                .getModel(Controller.this.lastClickedElement.getModel());
        if(model == null) {
            log.warn("problem! lastClickedElement: "
                    + Controller.this.lastClickedElement.toString());
        }
        Controller.this.notifyEditorPanel(model);
    }
    
    public void commit(XCommand addModelCommand, final XTransaction modelTransactions) {
        
        if(addModelCommand != null) {
            this.service.commitAddedModel(this.lastClickedElement, addModelCommand,
                    modelTransactions);
        } else {
            this.service.commitModelTransactions(this.lastClickedElement, modelTransactions);
        }
    }
    
    // public void notifySelectionTree(XAddress address) {
    // log.info("selection tree notified for address " + address.toString());
    // this.selectionTree.notifyMe(address);
    //
    // }
    
    public void displayError(String message) {
        this.warningWidget.display(message);
        
    }
    
    public void registerWarningWidget(WarningWidget warningWidget) {
        this.warningWidget = warningWidget;
    }
    
    public void registerTableController(TableController tableController) {
        this.tableController = tableController;
        
    }
    
    public void notifyTableController(XAddress eventLocation, Status status) {
        if(this.tableController == null) {
            SessionCachedModel model = DataModel.getInstance()
                    .getRepo(eventLocation.getRepository()).getModel(eventLocation.getModel());
            notifyEditorPanel(model);
        } else {
            this.tableController.notifyTable(eventLocation, status);
        }
    }
    
    public static SessionCachedModel getCurrentlySelectedModel() {
        XId currentRepo = instance.getSelectedModelAddress().getRepository();
        XId currenttModel = instance.getSelectedModelAddress().getModel();
        
        SessionCachedModel model = DataModel.getInstance().getRepo(currentRepo)
                .getModel(currenttModel);
        return model;
    }
    
    public void removeModel(final XAddress address) {
        this.service.removeModel(address);
        
    }
    
    public SelectionTree getSelectionTree() {
        
        return this.selectionTree;
    }
    
    public TableController getTableController() {
        return this.tableController;
    }
    
    public void open(XAddress address) {
        // TODO Auto-generated method stub
        
    }
    
    public void fetchModelIds(XAddress address, CompoundActionCallback compoundPresentationCallback) {
        this.service.getModelIdsFromServer(address, compoundPresentationCallback);
        
    }
    
    public void present() {
        this.selectionTree.build();
    }
    
    public void presentModel(XAddress address) {
        this.lastClickedElement = address;
        this.editorPanel.notifyMe(getCurrentlySelectedModel());
        
    }
}
