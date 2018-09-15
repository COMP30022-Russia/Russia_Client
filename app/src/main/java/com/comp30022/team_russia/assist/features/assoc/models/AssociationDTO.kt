package com.comp30022.team_russia.assist.features.assoc.models


/**
 * Data Transfer Object representing an Association object we get the from server. 
 */
open class AssociationDTO (
    var id: Int = 0,
    var user: UserResponseDTO? = null
)
