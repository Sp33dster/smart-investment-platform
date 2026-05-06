package com.speedster.investment.smart_investment_platform.asset.application.mapper;

import com.speedster.investment.smart_investment_platform.asset.application.dto.AssetResponse;
import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    @Mapping(target = "gainLoss", expression = "java(asset.calculateGainLoss())")
    @Mapping(target = "gainLossPercent", expression = "java(asset.calculateGainLossPercent())")
    @Mapping(target = "createdAt", source = "createdAt")
    AssetResponse toResponse(Asset asset);

    List<AssetResponse> toResponseList(List<Asset> assets);
}
