<dl
  class="grid
         grid-cols-2
         [&>:is(dt,dd)]:border-b
         [&>:is(dt,dd)]:px-4
         [&>:is(dt,dd)]:py-3
         [&>dd]:font-bold
         [&>dt]:after:content-[':']">
  <slot />
</dl>
