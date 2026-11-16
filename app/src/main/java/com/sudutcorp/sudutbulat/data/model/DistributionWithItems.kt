package com.sudutcorp.sudutbulat.data.model

/**
 * Composite data holder for a distribution header together with all its line items.
 */
data class DistributionWithItems(
    val distribution: MaterialDistribution,
    val items: List<MaterialDistributionItem>
)
