package mdpa.gdpr.analysis.dfd;

import java.io.IOException;
import java.util.Map;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

public record DataFlowDiagramAndDataDictionary(DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {
    public void save(String path) {
        URI uriDFD = URI.createURI(path + "test.dataflowdiagram");
        URI uriDD = URI.createURI(path + "test.datadictionary");
        Resource resourceDFD = new XMIResourceImpl(uriDFD);
        resourceDFD.getContents()
                .add(dataFlowDiagram);
        Resource resourceDD = new XMIResourceImpl(uriDD);
        resourceDD.getContents()
                .add(dataDictionary);
        try {
            resourceDFD.save(Map.of());
            resourceDD.save(Map.of());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
