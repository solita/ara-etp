import * as Maybe from '@Utility/maybe-utils';
import * as R from 'ramda';
import * as EtUtils from '@Pages/energiatodistus/energiatodistus-utils';

export const calculateCostFromValues = (consumption, price) => {
  const consumptionValue =
    consumption && Maybe.isMaybe(consumption)
      ? Maybe.orSome(null, consumption)
      : consumption;
  const priceValue =
    price && Maybe.isMaybe(price) ? Maybe.orSome(null, price) : price;

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

export const extractVaiheConsumptionValues = vaiheet =>
  vaiheet.map(vaihe =>
    R.compose(R.map(EtUtils.unnestValidation), R.defaultTo({}))(vaihe.tulokset)
  );

export const calculateVaiheCosts = (
  vaiheet,
  energiamuodot,
  vaiheConsumptionValues,
  priceValues
) =>
  vaiheet.map((_, vaiheIndex) =>
    calculateAllCosts(energiamuodot, energiamuoto => {
      const consumption =
        vaiheConsumptionValues[vaiheIndex][energiamuoto.consumptionField];
      const price = priceValues[energiamuoto.priceField];
      return calculateCostFromValues(consumption, price);
    })
  );

export const calculateVaiheDifferences = (vaiheTotalCosts, baselineTotalCost) =>
  vaiheTotalCosts.map((vaiheCost, index) => {
    const previousCost =
      index === 0 ? baselineTotalCost : vaiheTotalCosts[index - 1];
    return R.lift(R.subtract)(vaiheCost, previousCost);
  });
