import * as R from 'ramda';
import * as Maybe from '../../utils/maybe-utils';

const ENTER_KEY_CODE = 13;
const DOWN_ARROW_KEY_CODE = 40;
const UP_ARROW_KEY_CODE = 38;
const ESCAPE_KEY_CODE = 27;
const TAB_KEY_CODE = 9;

export const tabFocus = R.curry((nodes, shiftKey, focusIndex) =>
  shiftKey ? previousFocusOnTab(focusIndex) : nextFocusOnTab(nodes, focusIndex)
);

export const nextFocus = R.curry((nodes, focusIndex) => {
  return focusIndex === nodes.length - 1 ? 0 : focusIndex + 1;
});

export const previousFocus = R.curry((nodes, focusIndex) => {
  return focusIndex === 0 ? nodes.length - 1 : focusIndex - 1;
});

export const nextFocusOnTab = R.curry((nodes, focusIndex) =>
  focusIndex + 1 === nodes.length ? Maybe.None() : Maybe.Some(focusIndex + 1)
);

export const previousFocusOnTab = focusIndex =>
  focusIndex === 0 ? Maybe.None() : Maybe.Some(focusIndex - 1);

export const blurNode = R.curry((nodes, focusIndex) =>
  nodes[focusIndex].blur()
);

export const clickNode = R.curry((nodes, focusIndex) => {
  nodes[focusIndex].click();
  blurNode(nodes, focusIndex);
});

export const focusNode = R.curry((nodes, focusIndex) =>
  nodes[focusIndex].focus()
);

const keyHandlers = {
  [ENTER_KEY_CODE]: clickNode,
  [DOWN_ARROW_KEY_CODE]: R.curry((nodes, focusIndex) =>
    R.compose(R.tap(focusNode(nodes)), nextFocus(nodes))(focusIndex)
  ),
  [UP_ARROW_KEY_CODE]: R.curry((nodes, focusIndex) =>
    R.compose(R.tap(focusNode(nodes)), previousFocus(nodes))(focusIndex)
  ),
  [ESCAPE_KEY_CODE]: blurNode,
  [TAB_KEY_CODE]: R.curry((nodes, shiftKey, focusIndex) =>
    R.compose(
      Maybe.fold(null, R.tap(focusNode(nodes))),
      tabFocus(nodes, shiftKey)
    )(focusIndex)
  )
};

export const isHandlableKey = key =>
  R.compose(R.contains(R.toString(key)), R.keys)(keyHandlers);

export const handleKey = R.curry((nodes, event) => {
  const handler = R.prop(event.keyCode, keyHandlers);
  if (event.keyCode === TAB_KEY_CODE) {
    return handler(nodes, event.shiftKey);
  }
  return handler(nodes);
});
