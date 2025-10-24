import * as Maybe from '@Utility/maybe-utils';
import * as R from 'ramda';
import * as EtUtils from '@Pages/energiatodistus/energiatodistus-utils';

export const calculateCostFromValues = R.curry((consumption, price) =>
  R.lift((c, p) => (c * p) / 100)(consumption, price)
);

export const calculateAllCosts = (energiamuodot, calculateFn) =>
  energiamuodot.reduce((acc, energiamuoto) => {
    acc[energiamuoto.key] = calculateFn(energiamuoto);
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
