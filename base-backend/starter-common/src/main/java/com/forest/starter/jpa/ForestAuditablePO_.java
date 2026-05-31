package com.forest.starter.jpa;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

import java.time.LocalDateTime;

/**
 * Static metamodel for {@link ForestAuditablePO}.
 *
 * <p>Business modules generate entity metamodel classes in their own compilation unit.
 * Keeping this base metamodel in starter-common lets those generated classes extend it
 * without depending on a generated source from another module.</p>
 */
@StaticMetamodel(ForestAuditablePO.class)
public abstract class ForestAuditablePO_ {
    public static volatile SingularAttribute<ForestAuditablePO, Long> createdId;
    public static volatile SingularAttribute<ForestAuditablePO, Long> modifiedId;
    public static volatile SingularAttribute<ForestAuditablePO, Integer> deleted;
    public static volatile SingularAttribute<ForestAuditablePO, LocalDateTime> createdTime;
    public static volatile SingularAttribute<ForestAuditablePO, LocalDateTime> modifiedTime;

    public static final String CREATED_ID = "createdId";
    public static final String MODIFIED_ID = "modifiedId";
    public static final String DELETED = "deleted";
    public static final String CREATED_TIME = "createdTime";
    public static final String MODIFIED_TIME = "modifiedTime";

    protected ForestAuditablePO_() {
    }
}
