package com.ef.dao;

/**
 * Written By: Scott D. Pinkston Mar, 2018
 *
 * Allows decoupling of file parser from DAO layer via the Visitor pattern.
 */
public interface PersistenceVisitor {

    void visit( String[] columns );
}
