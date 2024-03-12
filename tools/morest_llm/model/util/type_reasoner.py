import loguru

from model.parameter import ParameterAttribute

logger = loguru.logger


def reason_type(
        producer_attribute: ParameterAttribute, consumer_attribute: ParameterAttribute
):
    # type is not the same
    if producer_attribute.parameter_type != consumer_attribute.parameter_type:
        return False

    # check enum
    if producer_attribute.schema_info.enum != consumer_attribute.schema_info.enum:
        return False

    # check format
    if producer_attribute.schema_info.format != consumer_attribute.schema_info.format:
        return False

    # check pattern
    if producer_attribute.schema_info.pattern != consumer_attribute.schema_info.pattern:
        return False

    # check maximum
    if producer_attribute.schema_info.maximum != consumer_attribute.schema_info.maximum:
        return False

    # check minimum
    if producer_attribute.schema_info.minimum != consumer_attribute.schema_info.minimum:
        return False
    logger.info(
        f"producer {producer_attribute.attribute_path} == consumer {consumer_attribute.attribute_path}"
    )

    return True
