import * as Maybe from '@Utility/maybe-utils';

/**
 * Helper function to unwrap Maybe values
 * @param {*} value - Value that may or may not be a Maybe
 * @returns {*} - Unwrapped value or the value itself if not a Maybe
 */
export const unwrapMaybe = value =>
  value && Maybe.isMaybe(value) ? Maybe.orSome(null, value) : value;

export const calculateCostFromValues = (consumption, price) => {
  const consumptionValue = unwrapMaybe(consumption);
  const priceValue = unwrapMaybe(price);

  if (consumptionValue == null || priceValue == null) {
    return null;
  }

  return (consumptionValue * priceValue) / 100;
};

export const calculateAllCosts = (energiamuodot, calculateFn) =>
  energiamuodot.reduce((acc, energiamuoto) => {
    const cost = calculateFn(energiamuoto);
    acc[energiamuoto.key] = cost !== null ? Maybe.Some(cost) : Maybe.None();
    return acc;
  }, {});
