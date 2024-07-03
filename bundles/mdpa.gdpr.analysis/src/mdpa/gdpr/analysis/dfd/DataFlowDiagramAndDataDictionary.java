package mdpa.gdpr.analysis.dfd;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import java.io.IOException;
import java.util.Map;

public record DataFlowDiagramAndDataDictionary(DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {
	public void save() {
		String path = "/home/felix/development/bachelors-workspace/GDPR-Analysis/tests/mdpa.gdpr.analysis.testmodels/models/Banking/";
		URI uriDFD = URI.createURI(path + "test.dataflowdiagram");
		URI uriDD = URI.createURI(path + "test.datadictionary");
		Resource resourceDFD = new XMIResourceImpl(uriDFD);
		resourceDFD.getContents().add(dataFlowDiagram);
		Resource resourceDD = new XMIResourceImpl(uriDD);
		resourceDD.getContents().add(dataDictionary);
        try {
            resourceDFD.save(Map.of());
			resourceDD.save(Map.of());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
