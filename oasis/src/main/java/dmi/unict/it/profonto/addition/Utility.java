/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.profonto.addition;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class Utility
  {
    private static OWLNamedIndividual createIndividualInBase(String indIriBase, OWLClass owlclass, OWLOntology ontology, OWLDataFactory datafactory) {
        OWLNamedIndividual individual = datafactory.getOWLNamedIndividual(indIriBase);
        ontology.addAxiom(datafactory.getOWLClassAssertionAxiom(owlclass, individual));
        return individual;
    }

    private static OWLNamedIndividual createIndividualInBase(String indIriBase, String classIriBase, OWLOntology ontology, OWLDataFactory datafactory) {
        OWLNamedIndividual individual = datafactory.getOWLNamedIndividual(indIriBase);
        ontology.addAxiom(datafactory.getOWLClassAssertionAxiom(datafactory.getOWLClass(classIriBase), individual));
        return individual;
    }

    private static void createObjectPropertyAssertionAxiom(String property, OWLNamedIndividual subject, OWLNamedIndividual object, OWLOntology ontology, OWLDataFactory datafactory) {
        ontology.addAxiom(datafactory.getOWLObjectPropertyAssertionAxiom(
                datafactory.getOWLObjectProperty(property), subject, object));
    }

    private static void createDataPropertyAssertionAxiom(String property, OWLNamedIndividual individual, String value, OWL2Datatype datatype, OWLOntology ontology, OWLDataFactory datafactory) {
        ontology.addAxiom(datafactory.getOWLDataPropertyAssertionAxiom(
                datafactory.getOWLDataProperty(property), individual,
                datafactory.getOWLLiteral(value, datatype)));
    }
  }
